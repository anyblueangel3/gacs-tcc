package br.uel.gacs.controller;

import br.uel.gacs.dao.ColunaDAO;
import br.uel.gacs.dao.CurvaDAO;
import br.uel.gacs.dao.DadoColunaDAO;
import br.uel.gacs.dao.ExperimentoDAO;
import br.uel.gacs.model.Coluna;
import br.uel.gacs.model.Curva;
import br.uel.gacs.model.DadoColuna;
import br.uel.gacs.model.Experimento;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Carrega curvas e calcula parâmetros de caracterização de um diodo. */
public final class CaracterizacaoDiodoCtlr {
    private static final double CONSTANTE_BOLTZMANN = 1.380649E-23;
    private static final double CARGA_ELEMENTAR = 1.602176634E-19;
    private static final double FRACAO_INICIO_JOELHO = 0.10;

    private final CurvaDAO curvaDAO;
    private final ColunaDAO colunaDAO;
    private final DadoColunaDAO dadoColunaDAO;
    private final ExperimentoDAO experimentoDAO;

    public CaracterizacaoDiodoCtlr() {
        this.curvaDAO = new CurvaDAO();
        this.colunaDAO = new ColunaDAO();
        this.dadoColunaDAO = new DadoColunaDAO();
        this.experimentoDAO = new ExperimentoDAO();
    }

    public Experimento buscarExperimento(Long idExperimento) throws SQLException {
        if (idExperimento == null) throw new IllegalArgumentException("O experimento deve estar salvo.");
        return experimentoDAO.buscarPorId(idExperimento)
                .orElseThrow(() -> new SQLException("O experimento não foi encontrado."));
    }

    public List<CurvaDisponivel> listarCurvas(Long idExperimento) throws SQLException {
        Map<Long, Coluna> colunas = new HashMap<>();
        for (Coluna coluna : colunaDAO.listarPorExperimento(idExperimento)) colunas.put(coluna.getId(), coluna);
        ArrayList<CurvaDisponivel> resultado = new ArrayList<>();
        for (Curva curva : curvaDAO.listarPorExperimento(idExperimento)) {
            Coluna x = colunas.get(curva.getIdColunaX());
            Coluna y = colunas.get(curva.getIdColunaY());
            if (x == null || y == null) continue;
            List<PontoDiodo> pontos = carregarPontos(curva);
            long diretos = pontos.stream().filter(p -> p.tensao() > 0 && p.corrente() > 0).count();
            long reversos = pontos.stream().filter(p -> p.tensao() < 0 && p.corrente() < 0).count();
            String perfil = diretos > reversos ? "predominantemente direta"
                    : reversos > diretos ? "predominantemente reversa" : "mista";
            resultado.add(new CurvaDisponivel(curva, x, y, pontos.size(), perfil));
        }
        return List.copyOf(resultado);
    }

    public ResultadoDiodo caracterizar(CurvaDisponivel direta, CurvaDisponivel reversa,
                                        double temperatura, double correnteReferencia,
                                        double tensaoMinima, double tensaoMaxima,
                                        double tensaoRetificacao,
                                        double correnteReferenciaRuptura,
                                        double tensaoMinimaFuga,
                                        double tensaoMaximaFuga,
                                        double correnteMinimaRs,
                                        double correnteMaximaRs) throws SQLException {
        if (direta == null) throw new IllegalArgumentException("Selecione a curva direta.");
        if (!Double.isFinite(temperatura) || temperatura <= 0)
            throw new IllegalArgumentException("A temperatura deve ser maior que zero kelvin.");
        if (!Double.isFinite(correnteReferencia) || correnteReferencia <= 0)
            throw new IllegalArgumentException("A corrente de referência deve ser positiva.");
        if (!Double.isFinite(tensaoMinima) || !Double.isFinite(tensaoMaxima)
                || tensaoMinima >= tensaoMaxima)
            throw new IllegalArgumentException("Informe um intervalo de tensão válido para o ajuste.");
        if (!Double.isFinite(tensaoRetificacao) || tensaoRetificacao <= 0)
            throw new IllegalArgumentException("A tensão da razão de retificação deve ser positiva.");
        if (reversa != null && (!Double.isFinite(correnteReferenciaRuptura) || correnteReferenciaRuptura <= 0))
            throw new IllegalArgumentException("A corrente de referência da ruptura deve ser positiva.");
        if (reversa != null && (!Double.isFinite(tensaoMinimaFuga) || !Double.isFinite(tensaoMaximaFuga)
                || tensaoMinimaFuga < 0 || tensaoMinimaFuga >= tensaoMaximaFuga))
            throw new IllegalArgumentException("Informe um intervalo válido para o módulo da tensão de fuga.");
        if (!Double.isFinite(correnteMinimaRs) || !Double.isFinite(correnteMaximaRs)
                || correnteMinimaRs <= 0 || correnteMinimaRs >= correnteMaximaRs)
            throw new IllegalArgumentException("Informe um intervalo de corrente válido para a resistência em série.");

        List<PontoDiodo> pontosDiretos = carregarPontos(direta.curva());
        int quantidadePontosDiretos = (int) pontosDiretos.stream()
                .filter(p -> p.tensao() > 0 && p.corrente() > 0)
                .count();
        List<PontoDiodo> ajuste = pontosDiretos.stream()
                .filter(p -> p.tensao() >= tensaoMinima && p.tensao() <= tensaoMaxima)
                .filter(p -> p.corrente() > 0 && Double.isFinite(Math.log(p.corrente())))
                .sorted(Comparator.comparingDouble(PontoDiodo::tensao)).toList();
        if (ajuste.size() < 3)
            throw new IllegalArgumentException("O intervalo selecionado deve conter ao menos três pontos com corrente positiva.");

        Regressao regressao = regressaoLinear(ajuste);
        if (regressao.inclinacao() <= 0)
            throw new IllegalArgumentException("A curva selecionada não apresenta crescimento exponencial no intervalo informado.");
        double tensaoTermica = CONSTANTE_BOLTZMANN * temperatura / CARGA_ELEMENTAR;
        double correnteSaturacao = Math.exp(regressao.intercepto());
        double fatorIdealidade = 1.0 / (regressao.inclinacao() * tensaoTermica);
        Double tensaoDireta = interpolarTensaoPorCorrente(pontosDiretos, correnteReferencia);
        Double resistenciaDinamica = resistenciaDinamica(pontosDiretos, correnteReferencia);
        EstimativaRs estimativaRs = estimarResistenciaSerie(pontosDiretos, fatorIdealidade,
                tensaoTermica, correnteMinimaRs, correnteMaximaRs);

        Double correnteReversaMedia = null;
        Double correnteReversaMaxima = null;
        Double razaoRetificacao = null;
        Double tensaoRuptura = null;
        Double correnteFugaMedia = null;
        Double correnteFugaMaxima = null;
        Double tensaoInicioJoelho = null;
        Double larguraJoelho = null;
        Double inclinacaoPosRuptura = null;
        Double resistenciaDinamicaPosRuptura = null;
        Double rQuadradoPosRuptura = null;
        int pontosReversos = 0;
        int pontosFuga = 0;
        int pontosPosRuptura = 0;
        if (reversa != null) {
            List<PontoDiodo> dadosReversos = carregarPontos(reversa.curva()).stream()
                    .filter(p -> p.tensao() < 0 && p.corrente() < 0).toList();
            if (dadosReversos.isEmpty())
                throw new IllegalArgumentException("A curva reversa selecionada não possui pontos com tensão e corrente negativas.");
            pontosReversos = dadosReversos.size();
            correnteReversaMedia = dadosReversos.stream().mapToDouble(p -> Math.abs(p.corrente())).average().orElseThrow();
            correnteReversaMaxima = dadosReversos.stream().mapToDouble(p -> Math.abs(p.corrente())).max().orElseThrow();
            tensaoRuptura = interpolarTensaoRuptura(dadosReversos, correnteReferenciaRuptura);
            Double inicioJoelho = interpolarTensaoRuptura(dadosReversos,
                    correnteReferenciaRuptura * FRACAO_INICIO_JOELHO);
            tensaoInicioJoelho = inicioJoelho;
            List<PontoDiodo> dadosFuga = dadosReversos.stream()
                    .filter(p -> entre(Math.abs(p.tensao()), tensaoMinimaFuga, tensaoMaximaFuga))
                    .toList();
            pontosFuga = dadosFuga.size();
            if (!dadosFuga.isEmpty()) {
                correnteFugaMedia = dadosFuga.stream()
                        .mapToDouble(p -> Math.abs(p.corrente())).average().orElseThrow();
                correnteFugaMaxima = dadosFuga.stream()
                        .mapToDouble(p -> Math.abs(p.corrente())).max().orElseThrow();
            }
            if (tensaoRuptura != null && tensaoInicioJoelho != null)
                larguraJoelho = Math.max(0, tensaoRuptura - tensaoInicioJoelho);

            AnalisePosRuptura posRuptura = analisarPosRuptura(dadosReversos,
                    correnteReferenciaRuptura, tensaoRuptura);
            inclinacaoPosRuptura = posRuptura.inclinacao();
            resistenciaDinamicaPosRuptura = posRuptura.resistenciaDinamica();
            rQuadradoPosRuptura = posRuptura.rQuadrado();
            pontosPosRuptura = posRuptura.quantidadePontos();
            Double correnteDiretaNaTensao = interpolarCorrentePorTensao(pontosDiretos, tensaoRetificacao);
            Double correnteReversaNaTensao = interpolarCorrentePorTensao(dadosReversos, -tensaoRetificacao);
            if (correnteDiretaNaTensao != null && correnteReversaNaTensao != null
                    && Math.abs(correnteReversaNaTensao) > 0) {
                razaoRetificacao = Math.abs(correnteDiretaNaTensao) / Math.abs(correnteReversaNaTensao);
            }
        }

        return new ResultadoDiodo(quantidadePontosDiretos, ajuste.size(), pontosReversos,
                tensaoMinima, tensaoMaxima, temperatura, correnteReferencia, tensaoRetificacao,
                correnteReferenciaRuptura, tensaoMinimaFuga, tensaoMaximaFuga,
                correnteMinimaRs, correnteMaximaRs,
                tensaoDireta, correnteSaturacao, fatorIdealidade, resistenciaDinamica,
                regressao.rQuadrado(), correnteReversaMedia, correnteReversaMaxima,
                correnteFugaMedia, correnteFugaMaxima, pontosFuga,
                razaoRetificacao, tensaoRuptura, tensaoInicioJoelho, larguraJoelho,
                inclinacaoPosRuptura, resistenciaDinamicaPosRuptura,
                rQuadradoPosRuptura, pontosPosRuptura,
                estimativaRs.valor(), estimativaRs.quantidadePontos());
    }

    public String gerarRelatorio(Experimento experimento, CurvaDisponivel direta,
                                  CurvaDisponivel reversa, ResultadoDiodo r) {
        StringBuilder texto = new StringBuilder();
        texto.append("RELATÓRIO DE CARACTERIZAÇÃO DE DIODO\n\n")
                .append("Experimento: ").append(experimento.getNomeExperimento()).append('\n')
                .append("Curva direta: ").append(direta.curva().getNome()).append('\n')
                .append("Curva reversa/ruptura: ").append(reversa == null ? "não informada" : reversa.curva().getNome()).append('\n')
                .append("Temperatura considerada: ").append(formatar(r.temperatura())).append(" K\n\n")
                .append("DADOS E INTERVALO DE ANÁLISE\n")
                .append("Pontos da curva direta: ").append(r.pontosDiretos()).append('\n')
                .append("Pontos utilizados no ajuste: ").append(r.pontosAjuste()).append('\n')
                .append("Região do ajuste: ").append(formatar(r.tensaoMinima())).append(" V a ")
                .append(formatar(r.tensaoMaxima())).append(" V\n\n")
                .append("PARÂMETROS DA POLARIZAÇÃO DIRETA\n")
                .append("Tensão direta em ").append(formatar(r.correnteReferencia())).append(" A: ")
                .append(formatarOpcional(r.tensaoDireta(), " V")).append('\n')
                .append("Corrente de saturação I_s: ").append(formatar(r.correnteSaturacao())).append(" A\n")
                .append("Fator de idealidade n: ").append(formatar(r.fatorIdealidade())).append('\n')
                .append("Tensão térmica V_T: ").append(formatar(tensaoTermica(r))).append(" V\n")
                .append("Resistência dinâmica direta em ").append(formatar(r.correnteReferencia())).append(" A: ")
                .append(formatarOpcional(r.resistenciaDinamica(), " ohm")).append('\n')
                .append("Resistência em série R_s: ").append(formatarOpcional(r.resistenciaSerie(), " ohm")).append('\n')
                .append("Faixa de corrente para R_s: ").append(formatar(r.correnteMinimaRs())).append(" A a ")
                .append(formatar(r.correnteMaximaRs())).append(" A\n")
                .append("Pontos utilizados na estimativa de R_s: ").append(r.pontosResistenciaSerie()).append('\n')
                .append("Coeficiente de determinação R²: ").append(formatar(r.rQuadrado())).append("\n\n");
        if (reversa != null) {
            texto.append("PARÂMETROS DA POLARIZAÇÃO REVERSA\n")
                    .append("Pontos reversos utilizados: ").append(r.pontosReversos()).append('\n')
                    .append("Módulo médio da corrente em toda a região reversa: ")
                    .append(formatarOpcional(r.correnteReversaMedia(), " A")).append('\n')
                    .append("Módulo máximo da corrente em toda a região reversa: ")
                    .append(formatarOpcional(r.correnteReversaMaxima(), " A")).append('\n')
                    .append("\nREGIÃO DE FUGA ANTERIOR AO JOELHO\n")
                    .append("Intervalo informado para |V_R|: ")
                    .append(formatar(r.tensaoMinimaFuga())).append(" V a ")
                    .append(formatar(r.tensaoMaximaFuga())).append(" V\n")
                    .append("Pontos utilizados na corrente de fuga: ").append(r.pontosFuga()).append('\n')
                    .append("Módulo médio da corrente de fuga: ")
                    .append(formatarOpcional(r.correnteFugaMedia(), " A")).append('\n')
                    .append("Módulo máximo da corrente de fuga: ")
                    .append(formatarOpcional(r.correnteFugaMaxima(), " A")).append("\n\n")
                    .append("JOELHO E REGIÃO DE RUPTURA\n")
                    .append("Início operacional do joelho V_K em |I_R| = ")
                    .append(formatar(r.correnteReferenciaRuptura() * FRACAO_INICIO_JOELHO)).append(" A: ")
                    .append(formatarOpcional(r.tensaoInicioJoelho(), " V")).append('\n')
                    .append("Tensão de ruptura V_BR em |I_R| = ")
                    .append(formatar(r.correnteReferenciaRuptura())).append(" A: ")
                    .append(r.tensaoRuptura() == null
                            ? "não disponível: a curva não alcança essa corrente de referência"
                            : formatar(r.tensaoRuptura()) + " V")
                    .append('\n')
                    .append("Largura operacional do joelho (V_BR - V_K): ")
                    .append(formatarOpcional(r.larguraJoelho(), " V")).append('\n')
                    .append("Pontos utilizados no ajuste pós-ruptura: ").append(r.pontosPosRuptura()).append('\n')
                    .append("Inclinação pós-ruptura d|I_R|/d|V_R|: ")
                    .append(formatarOpcional(r.inclinacaoPosRuptura(), " S")).append('\n')
                    .append("Resistência dinâmica efetiva pós-ruptura d|V_R|/d|I_R|: ")
                    .append(formatarOpcional(r.resistenciaDinamicaPosRuptura(), " ohm")).append('\n')
                    .append("Coeficiente de determinação R² pós-ruptura: ")
                    .append(formatarOpcional(r.rQuadradoPosRuptura(), "")).append('\n')
                    .append("Razão de retificação em ±").append(formatar(r.tensaoRetificacao())).append(" V: ")
                    .append(formatarOpcional(r.razaoRetificacao(), "")).append("\n\n");
        }
        texto.append("MÉTODO\n")
                .append("Os parâmetros I_s e n foram estimados por regressão linear de ln(I) em função de V, ")
                .append("usando a equação de Shockley na região direta selecionada. R² mede a qualidade desse ajuste. ")
                .append("A tensão térmica é V_T = kT/q. R_s representa a resistência em série equivalente ")
                .append("do diodo e foi estimada pela média de dV/dI - nV_T/I na faixa de corrente informada. ")
                .append("A tensão de ruptura V_BR é o módulo da tensão reversa interpolada no nível de corrente ")
                .append("de referência. O início operacional do joelho V_K foi definido em 10% desse nível. ")
                .append("A corrente de fuga foi caracterizada no intervalo de módulo da tensão reversa ")
                .append("informado pelo operador, que deve representar a região anterior ao joelho. ")
                .append("Na região pós-ruptura, a inclinação foi obtida por regressão linear de |I_R| em função ")
                .append("de |V_R|. A resistência dinâmica efetiva pós-ruptura corresponde ao inverso da ")
                .append("inclinação média ajustada nessa faixa e não a uma derivada local em um único ponto.\n\n")
                .append("OBSERVAÇÃO\n")
                .append("Os resultados são estimativas dependentes da qualidade dos dados, da temperatura informada ")
                .append("e dos intervalos escolhidos. A análise de histerese exige varreduras identificáveis de ida ")
                .append("e volta e não é inferida de uma única sequência de dados.");
        return texto.toString();
    }

    private List<PontoDiodo> carregarPontos(Curva curva) throws SQLException {
        Map<Integer, Double> tensoes = new HashMap<>();
        for (DadoColuna dado : dadoColunaDAO.listarPorColuna(curva.getIdColunaX()))
            tensoes.put(dado.getNumeroDaMedida(), dado.getValorMedida());
        ArrayList<PontoDiodo> pontos = new ArrayList<>();
        for (DadoColuna dado : dadoColunaDAO.listarPorColuna(curva.getIdColunaY())) {
            Double tensao = tensoes.get(dado.getNumeroDaMedida());
            double corrente = dado.getValorMedida();
            if (tensao != null && Double.isFinite(tensao) && Double.isFinite(corrente))
                pontos.add(new PontoDiodo(tensao, corrente));
        }
        pontos.sort(Comparator.comparingDouble(PontoDiodo::tensao));
        return List.copyOf(pontos);
    }

    private Regressao regressaoLinear(List<PontoDiodo> pontos) {
        int n = pontos.size();
        double somaX = 0, somaY = 0, somaXX = 0, somaXY = 0;
        for (PontoDiodo p : pontos) {
            double x = p.tensao(), y = Math.log(p.corrente());
            somaX += x; somaY += y; somaXX += x * x; somaXY += x * y;
        }
        double denominador = n * somaXX - somaX * somaX;
        if (Math.abs(denominador) < 1E-30)
            throw new IllegalArgumentException("As tensões do intervalo não permitem realizar a regressão.");
        double inclinacao = (n * somaXY - somaX * somaY) / denominador;
        double intercepto = (somaY - inclinacao * somaX) / n;
        double mediaY = somaY / n, ssTotal = 0, ssResiduos = 0;
        for (PontoDiodo p : pontos) {
            double y = Math.log(p.corrente());
            double previsto = intercepto + inclinacao * p.tensao();
            ssTotal += Math.pow(y - mediaY, 2);
            ssResiduos += Math.pow(y - previsto, 2);
        }
        double r2 = ssTotal == 0 ? 1 : 1 - ssResiduos / ssTotal;
        return new Regressao(inclinacao, intercepto, r2);
    }

    private Double interpolarTensaoPorCorrente(List<PontoDiodo> pontos, double corrente) {
        List<PontoDiodo> diretos = pontos.stream().filter(p -> p.tensao() >= 0 && p.corrente() >= 0)
                .sorted(Comparator.comparingDouble(PontoDiodo::tensao)).toList();
        for (int i = 1; i < diretos.size(); i++) {
            PontoDiodo a = diretos.get(i - 1), b = diretos.get(i);
            if (entre(corrente, a.corrente(), b.corrente()) && b.corrente() != a.corrente())
                return a.tensao() + (corrente - a.corrente()) * (b.tensao() - a.tensao())
                        / (b.corrente() - a.corrente());
        }
        return null;
    }

    private Double interpolarCorrentePorTensao(List<PontoDiodo> pontos, double tensao) {
        List<PontoDiodo> ordenados = pontos.stream().sorted(Comparator.comparingDouble(PontoDiodo::tensao)).toList();
        for (int i = 1; i < ordenados.size(); i++) {
            PontoDiodo a = ordenados.get(i - 1), b = ordenados.get(i);
            if (entre(tensao, a.tensao(), b.tensao()) && b.tensao() != a.tensao())
                return a.corrente() + (tensao - a.tensao()) * (b.corrente() - a.corrente())
                        / (b.tensao() - a.tensao());
        }
        return ordenados.stream().filter(p -> p.tensao() == tensao).map(PontoDiodo::corrente).findFirst().orElse(null);
    }

    /**
     * Obtém o módulo da tensão reversa no nível de corrente especificado.
     * A interpolação é realizada entre pontos consecutivos na varredura de tensão;
     * retorna {@code null} quando os dados não alcançam a corrente de referência.
     */
    private Double interpolarTensaoRuptura(List<PontoDiodo> pontos, double correnteReferencia) {
        List<PontoDiodo> reversos = pontos.stream()
                .filter(p -> p.tensao() < 0 && p.corrente() < 0)
                .sorted(Comparator.comparingDouble(p -> Math.abs(p.tensao())))
                .toList();
        for (PontoDiodo ponto : reversos) {
            if (Math.abs(ponto.corrente()) == correnteReferencia)
                return Math.abs(ponto.tensao());
        }
        for (int i = 1; i < reversos.size(); i++) {
            PontoDiodo a = reversos.get(i - 1), b = reversos.get(i);
            double correnteA = Math.abs(a.corrente());
            double correnteB = Math.abs(b.corrente());
            if (entre(correnteReferencia, correnteA, correnteB) && correnteB != correnteA) {
                double tensao = Math.abs(a.tensao())
                        + (correnteReferencia - correnteA)
                        * (Math.abs(b.tensao()) - Math.abs(a.tensao()))
                        / (correnteB - correnteA);
                return Double.isFinite(tensao) ? Math.abs(tensao) : null;
            }
        }
        return null;
    }

    private Double resistenciaDinamica(List<PontoDiodo> pontos, double corrente) {
        List<PontoDiodo> diretos = pontos.stream().filter(p -> p.tensao() >= 0 && p.corrente() >= 0)
                .sorted(Comparator.comparingDouble(PontoDiodo::tensao)).toList();
        if (diretos.size() < 2) return null;
        int proximo = 0;
        for (int i = 1; i < diretos.size(); i++)
            if (Math.abs(diretos.get(i).corrente() - corrente)
                    < Math.abs(diretos.get(proximo).corrente() - corrente)) proximo = i;
        int anterior = proximo == 0 ? 0 : proximo - 1;
        int posterior = proximo == diretos.size() - 1 ? proximo : proximo + 1;
        if (anterior == posterior) return null;
        double deltaI = diretos.get(posterior).corrente() - diretos.get(anterior).corrente();
        return deltaI == 0 ? null : Math.abs((diretos.get(posterior).tensao()
                - diretos.get(anterior).tensao()) / deltaI);
    }

    private EstimativaRs estimarResistenciaSerie(List<PontoDiodo> pontos, double fatorIdealidade,
                                                   double tensaoTermica, double correnteMinima,
                                                   double correnteMaxima) {
        List<PontoDiodo> diretos = pontos.stream()
                .filter(p -> p.tensao() > 0 && p.corrente() > 0)
                .sorted(Comparator.comparingDouble(PontoDiodo::corrente))
                .toList();
        if (diretos.size() < 3) return new EstimativaRs(null, 0);

        double soma = 0;
        int quantidade = 0;
        for (int i = 1; i < diretos.size() - 1; i++) {
            PontoDiodo ponto = diretos.get(i);
            if (ponto.corrente() < correnteMinima || ponto.corrente() > correnteMaxima) continue;
            PontoDiodo anterior = diretos.get(i - 1);
            PontoDiodo posterior = diretos.get(i + 1);
            double deltaI = posterior.corrente() - anterior.corrente();
            if (deltaI == 0) continue;
            double derivada = (posterior.tensao() - anterior.tensao()) / deltaI;
            double resistenciaSerie = derivada - fatorIdealidade * tensaoTermica / ponto.corrente();
            if (Double.isFinite(resistenciaSerie) && resistenciaSerie >= 0) {
                soma += resistenciaSerie;
                quantidade++;
            }
        }
        return quantidade == 0 ? new EstimativaRs(null, 0)
                : new EstimativaRs(soma / quantidade, quantidade);
    }

    private AnalisePosRuptura analisarPosRuptura(List<PontoDiodo> pontos,
                                                  double correnteReferencia,
                                                  Double tensaoRuptura) {
        if (tensaoRuptura == null) return AnalisePosRuptura.indisponivel();
        List<PontoDiodo> posRuptura = pontos.stream()
                .filter(p -> Math.abs(p.corrente()) >= correnteReferencia)
                .filter(p -> Math.abs(p.tensao()) >= tensaoRuptura)
                .sorted(Comparator.comparingDouble(p -> Math.abs(p.tensao())))
                .toList();
        if (posRuptura.size() < 3) return AnalisePosRuptura.indisponivel();

        RegressaoXY regressao = regressaoLinearXY(posRuptura);
        if (!Double.isFinite(regressao.inclinacao()) || regressao.inclinacao() <= 0)
            return AnalisePosRuptura.indisponivel();
        return new AnalisePosRuptura(regressao.inclinacao(), 1.0 / regressao.inclinacao(),
                regressao.rQuadrado(), posRuptura.size());
    }

    private RegressaoXY regressaoLinearXY(List<PontoDiodo> pontos) {
        int n = pontos.size();
        double somaX = 0, somaY = 0, somaXX = 0, somaXY = 0;
        for (PontoDiodo p : pontos) {
            double x = Math.abs(p.tensao()), y = Math.abs(p.corrente());
            somaX += x; somaY += y; somaXX += x * x; somaXY += x * y;
        }
        double denominador = n * somaXX - somaX * somaX;
        if (Math.abs(denominador) < 1E-30) return new RegressaoXY(Double.NaN, Double.NaN);
        double inclinacao = (n * somaXY - somaX * somaY) / denominador;
        double intercepto = (somaY - inclinacao * somaX) / n;
        double mediaY = somaY / n, ssTotal = 0, ssResiduos = 0;
        for (PontoDiodo p : pontos) {
            double y = Math.abs(p.corrente());
            double previsto = intercepto + inclinacao * Math.abs(p.tensao());
            ssTotal += Math.pow(y - mediaY, 2);
            ssResiduos += Math.pow(y - previsto, 2);
        }
        double r2 = ssTotal == 0 ? 1 : 1 - ssResiduos / ssTotal;
        return new RegressaoXY(inclinacao, r2);
    }

    private boolean entre(double valor, double a, double b) {
        return valor >= Math.min(a, b) && valor <= Math.max(a, b);
    }

    private String formatar(double valor) { return String.format(Locale.ROOT, "%.6E", valor); }
    private String formatarOpcional(Double valor, String unidade) {
        return valor == null || !Double.isFinite(valor) ? "não disponível no intervalo dos dados" : formatar(valor) + unidade;
    }

    public record CurvaDisponivel(Curva curva, Coluna colunaX, Coluna colunaY,
                                  int quantidadePontos, String perfil) { }
    public record PontoDiodo(double tensao, double corrente) { }
    public record ResultadoDiodo(int pontosDiretos, int pontosAjuste, int pontosReversos,
                                 double tensaoMinima, double tensaoMaxima, double temperatura,
                                 double correnteReferencia, double tensaoRetificacao,
                                 double correnteReferenciaRuptura,
                                 double tensaoMinimaFuga, double tensaoMaximaFuga,
                                 double correnteMinimaRs, double correnteMaximaRs,
                                 Double tensaoDireta, double correnteSaturacao,
                                 double fatorIdealidade, Double resistenciaDinamica,
                                 double rQuadrado, Double correnteReversaMedia,
                                 Double correnteReversaMaxima,
                                 Double correnteFugaMedia, Double correnteFugaMaxima,
                                 int pontosFuga, Double razaoRetificacao,
                                 Double tensaoRuptura, Double tensaoInicioJoelho,
                                 Double larguraJoelho, Double inclinacaoPosRuptura,
                                 Double resistenciaDinamicaPosRuptura,
                                 Double rQuadradoPosRuptura, int pontosPosRuptura,
                                 Double resistenciaSerie,
                                 int pontosResistenciaSerie) { }
    private double tensaoTermica(ResultadoDiodo r) {
        return CONSTANTE_BOLTZMANN * r.temperatura() / CARGA_ELEMENTAR;
    }
    private record Regressao(double inclinacao, double intercepto, double rQuadrado) { }
    private record RegressaoXY(double inclinacao, double rQuadrado) { }
    private record EstimativaRs(Double valor, int quantidadePontos) { }
    private record AnalisePosRuptura(Double inclinacao, Double resistenciaDinamica,
                                     Double rQuadrado, int quantidadePontos) {
        private static AnalisePosRuptura indisponivel() {
            return new AnalisePosRuptura(null, null, null, 0);
        }
    }
}
