package br.uel.gacs.controller;

import br.uel.gacs.application.Autorizador;
import br.uel.gacs.application.SessaoUsuario;
import br.uel.gacs.dao.ColunaDAO;
import br.uel.gacs.dao.CurvaDAO;
import br.uel.gacs.dao.CurvaFetDAO;
import br.uel.gacs.dao.DadoColunaDAO;
import br.uel.gacs.dao.ExperimentoDAO;
import br.uel.gacs.model.Coluna;
import br.uel.gacs.model.Curva;
import br.uel.gacs.model.CurvaFet;
import br.uel.gacs.model.DadoColuna;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.TipoCurvaFet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Executa os cálculos genéricos da primeira caracterização de FET. */
public final class CaracterizacaoFetCtlr {
    private static final int MINIMO_PONTOS = 3;
    private static final double LIMITE_R_QUADRADO = 0.95;
    private static final double TOLERANCIA_ABSOLUTA = 1.0e-15;
    private static final double TOLERANCIA_RELATIVA = 1.0e-12;

    private final CurvaDAO curvaDAO = new CurvaDAO();
    private final CurvaFetDAO curvaFetDAO = new CurvaFetDAO();
    private final ColunaDAO colunaDAO = new ColunaDAO();
    private final DadoColunaDAO dadoColunaDAO = new DadoColunaDAO();
    private final ExperimentoDAO experimentoDAO = new ExperimentoDAO();

    public Experimento buscarExperimento(Long idExperimento) throws SQLException {
        if (idExperimento == null) {
            throw new IllegalArgumentException("O experimento deve estar salvo.");
        }
        return experimentoDAO.buscarPorId(idExperimento)
                .orElseThrow(() -> new SQLException("O experimento não foi encontrado."));
    }

    public List<CurvaDisponivel> listarCurvas(Long idExperimento) throws SQLException {
        Map<Long, Coluna> colunas = new HashMap<>();
        for (Coluna coluna : colunaDAO.listarPorExperimento(idExperimento)) {
            colunas.put(coluna.getId(), coluna);
        }

        List<CurvaDisponivel> resultado = new ArrayList<>();
        for (Curva curva : curvaDAO.listarPorExperimento(idExperimento)) {
            Coluna colunaX = colunas.get(curva.getIdColunaX());
            Coluna colunaY = colunas.get(curva.getIdColunaY());
            if (colunaX == null || colunaY == null) {
                continue;
            }
            Optional<CurvaFet> configuracao = curvaFetDAO.buscarPorCurva(curva.getId());
            resultado.add(new CurvaDisponivel(curva, colunaX, colunaY,
                    configuracao.orElse(null), carregarPontos(curva).size()));
        }
        return List.copyOf(resultado);
    }

    public CurvaFet salvarConfiguracao(Long idExperimento, Curva curva,
                                       TipoCurvaFet tipo, double tensaoConstante)
            throws SQLException {
        if (curva == null || curva.getId() == null) {
            throw new IllegalArgumentException("Selecione uma curva salva.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Selecione o tipo da curva de FET.");
        }
        validarFinito(tensaoConstante, "A tensão constante");
        exigirPermissao(idExperimento);

        Curva persistida = curvaDAO.buscarPorId(curva.getId())
                .orElseThrow(() -> new SQLException("A curva não foi encontrada."));
        Coluna colunaX = colunaDAO.buscarPorId(persistida.getIdColunaX())
                .orElseThrow(() -> new SQLException("A coluna X não foi encontrada."));
        Coluna colunaY = colunaDAO.buscarPorId(persistida.getIdColunaY())
                .orElseThrow(() -> new SQLException("A coluna Y não foi encontrada."));
        if (!idExperimento.equals(colunaX.getIdExperimento())
                || !idExperimento.equals(colunaY.getIdExperimento())) {
            throw new IllegalArgumentException(
                    "A curva selecionada não pertence ao experimento aberto.");
        }

        CurvaFet curvaFet = new CurvaFet(curva.getId(), tipo, tensaoConstante);
        if (curvaFetDAO.buscarPorCurva(curva.getId()).isPresent()) {
            if (!curvaFetDAO.atualizar(curvaFet)) {
                throw new SQLException("Não foi possível atualizar a configuração da curva.");
            }
        } else {
            curvaFetDAO.inserir(curvaFet);
        }
        return curvaFet;
    }

    public List<PontoFet> carregarPontos(Curva curva) throws SQLException {
        if (curva == null || curva.getIdColunaX() == null || curva.getIdColunaY() == null) {
            throw new IllegalArgumentException("Selecione uma curva válida.");
        }
        Map<Integer, Double> valoresX = new HashMap<>();
        for (DadoColuna dado : dadoColunaDAO.listarPorColuna(curva.getIdColunaX())) {
            valoresX.put(dado.getNumeroDaMedida(), dado.getValorMedida());
        }
        List<PontoFet> pontos = new ArrayList<>();
        for (DadoColuna dadoY : dadoColunaDAO.listarPorColuna(curva.getIdColunaY())) {
            Double x = valoresX.get(dadoY.getNumeroDaMedida());
            Double y = dadoY.getValorMedida();
            if (x != null && y != null && Double.isFinite(x) && Double.isFinite(y)) {
                pontos.add(new PontoFet(x, y));
            }
        }
        return List.copyOf(pontos);
    }

    private void exigirPermissao(Long idExperimento) throws SQLException {
        if (idExperimento == null) {
            throw new IllegalArgumentException("O experimento deve estar salvo.");
        }
        if (Autorizador.usuarioAtualEhAdministrador()) {
            return;
        }
        Long idUsuario = SessaoUsuario.exigirUsuarioLogado().getId();
        boolean permitido = experimentoDAO.buscarPorId(idExperimento)
                .map(experimento -> idUsuario.equals(experimento.getIdUsuario()))
                .orElse(false);
        if (!permitido) {
            throw new SecurityException(
                    "Somente o proprietário ou um administrador pode configurar curvas de FET.");
        }
    }

    public ResultadoSaida analisarSaida(List<PontoFet> pontos,
                                         double tensaoMinimaOhmica,
                                         double tensaoMaximaOhmica,
                                         double tensaoMinimaSaturacao,
                                         double tensaoMaximaSaturacao) {
        validarIntervalo(tensaoMinimaOhmica, tensaoMaximaOhmica,
                "região ôhmica");
        validarIntervalo(tensaoMinimaSaturacao, tensaoMaximaSaturacao,
                "região de saturação");

        List<PontoFet> pontosValidos = prepararPontos(pontos);
        List<PontoFet> pontosOhmicos = selecionarIntervalo(
                pontosValidos, tensaoMinimaOhmica, tensaoMaximaOhmica);
        List<PontoFet> pontosSaturacao = selecionarIntervalo(
                pontosValidos, tensaoMinimaSaturacao, tensaoMaximaSaturacao);

        AjusteLinear ajusteOhmico = ajustar(pontosOhmicos, "região ôhmica");
        AjusteLinear ajusteSaturacao = ajustar(pontosSaturacao,
                "região de saturação");

        List<String> advertencias = new ArrayList<>();
        adicionarAdvertenciasAjuste(advertencias, ajusteOhmico, "região ôhmica");
        adicionarAdvertenciasAjuste(advertencias, ajusteSaturacao,
                "região de saturação");

        Double resistenciaOhmica = inversoDaInclinacao(
                ajusteOhmico.inclinacao(), "R_DS,ômica", advertencias);
        Double resistenciaSaida = inversoDaInclinacao(
                ajusteSaturacao.inclinacao(), "r_o", advertencias);

        PontoFet joelho = calcularJoelho(ajusteOhmico, ajusteSaturacao,
                pontosValidos, advertencias);

        return new ResultadoSaida(
                pontosValidos.size(),
                pontosOhmicos.size(),
                pontosSaturacao.size(),
                tensaoMinimaOhmica,
                tensaoMaximaOhmica,
                tensaoMinimaSaturacao,
                tensaoMaximaSaturacao,
                ajusteOhmico,
                ajusteSaturacao,
                ajusteOhmico.inclinacao(),
                resistenciaOhmica,
                ajusteSaturacao.inclinacao(),
                resistenciaSaida,
                joelho,
                List.copyOf(advertencias));
    }

    public List<ResultadoCurvaSaida> analisarFamiliaSaida(
            List<ParametrosCurvaSaida> parametros) throws SQLException {
        if (parametros == null || parametros.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selecione pelo menos uma curva de saída para analisar.");
        }
        for (ParametrosCurvaSaida item : parametros) {
            if (item == null || item.curva() == null
                    || item.curva().configuracao() == null
                    || item.curva().configuracao().getTipoCurvaFet()
                            != TipoCurvaFet.SAIDA) {
                throw new IllegalArgumentException(
                        "A seleção contém uma curva que não é de saída.");
            }
        }

        List<ResultadoCurvaSaida> resultados = new ArrayList<>();
        for (ParametrosCurvaSaida item : parametros.stream()
                .sorted(Comparator.comparingDouble(parametro ->
                        parametro.curva().configuracao()
                                .getValorTensaoConstante()))
                .toList()) {
            ResultadoSaida resultado = analisarSaida(
                    carregarPontos(item.curva().curva()),
                    item.tensaoMinimaOhmica(),
                    item.tensaoMaximaOhmica(),
                    item.tensaoMinimaSaturacao(),
                    item.tensaoMaximaSaturacao());
            resultados.add(new ResultadoCurvaSaida(item.curva(), resultado));
        }
        return List.copyOf(resultados);
    }

    public ResultadoTransferencia analisarTransferenciaPorIntervalo(
            List<PontoFet> pontos, double tensaoMinima, double tensaoMaxima) {
        validarIntervalo(tensaoMinima, tensaoMaxima, "transcondutância");

        List<PontoFet> pontosValidos = prepararPontos(pontos);
        List<PontoFet> selecionados = selecionarIntervalo(
                pontosValidos, tensaoMinima, tensaoMaxima);
        AjusteLinear ajuste = ajustar(selecionados, "transcondutância");

        List<String> advertencias = new ArrayList<>();
        adicionarAdvertenciasAjuste(advertencias, ajuste, "transcondutância");
        if (ajuste.inclinacao() < 0.0) {
            advertencias.add("A transcondutância calculada é negativa.");
        }

        return new ResultadoTransferencia(
                pontosValidos.size(),
                selecionados.size(),
                tensaoMinima,
                tensaoMaxima,
                ajuste.inclinacao(),
                ajuste,
                List.copyOf(advertencias));
    }

    public List<ResultadoGmLocal> calcularTranscondutanciaLocal(
            List<PontoFet> pontos) {
        List<PontoFet> pontosValidos = prepararPontos(pontos);
        if (pontosValidos.size() < MINIMO_PONTOS) {
            throw new IllegalArgumentException(
                    "São necessários pelo menos três pontos para calcular g_m local.");
        }

        List<ResultadoGmLocal> resultados = new ArrayList<>();
        for (int indice = 1; indice < pontosValidos.size() - 1; indice++) {
            List<PontoFet> janela = List.of(
                    pontosValidos.get(indice - 1),
                    pontosValidos.get(indice),
                    pontosValidos.get(indice + 1));
            AjusteLinear ajuste = ajustar(janela, "janela local de g_m");
            List<String> advertencias = new ArrayList<>();
            adicionarAdvertenciasAjuste(advertencias, ajuste,
                    "janela local de g_m");
            if (ajuste.inclinacao() < 0.0) {
                advertencias.add("A transcondutância local calculada é negativa.");
            }
            resultados.add(new ResultadoGmLocal(
                    pontosValidos.get(indice).tensao(),
                    ajuste.inclinacao(),
                    ajuste,
                    List.copyOf(advertencias)));
        }
        return List.copyOf(resultados);
    }

    public ResultadoGanhoIntrinseco calcularGanhoIntrinseco(
            double gm, double vgsDoGm, double vdsDaTransferencia,
            double ro, double vgsDaSaida,
            double vdsMinimoSaturacao, double vdsMaximoSaturacao,
            boolean resultadoLocal) {
        validarFinito(gm, "g_m");
        validarFinito(vgsDoGm, "V_GS de g_m");
        validarFinito(vdsDaTransferencia, "V_DS da curva de transferência");
        if (Double.isNaN(ro)) {
            throw new IllegalArgumentException("r_o não pode ser NaN.");
        }
        validarFinito(vgsDaSaida, "V_GS da curva de saída");
        validarIntervalo(vdsMinimoSaturacao, vdsMaximoSaturacao,
                "saturação usada em r_o");

        if (!aproximadamenteIguais(vgsDoGm, vgsDaSaida)) {
            throw new IllegalArgumentException(
                    "Os valores de V_GS de g_m e r_o não coincidem.");
        }
        if (vdsDaTransferencia < vdsMinimoSaturacao
                || vdsDaTransferencia > vdsMaximoSaturacao) {
            throw new IllegalArgumentException(
                    "O V_DS de g_m não pertence ao intervalo usado para calcular r_o.");
        }

        double ganho = gm * ro;
        double ganhoDb = ganho == 0.0
                ? Double.NEGATIVE_INFINITY
                : 20.0 * Math.log10(Math.abs(ganho));
        return new ResultadoGanhoIntrinseco(
                ganho, ganhoDb, resultadoLocal ? "LOCAL" : "EFETIVO");
    }

    public ResultadoGanhoIntegrado analisarGanhoIntrinseco(
            CurvaDisponivel curvaTransferencia,
            List<CurvaDisponivel> curvas,
            double vgsReferencia,
            double gmMinimo,
            double gmMaximo,
            double vdsMinimoSaturacao,
            double vdsMaximoSaturacao,
            boolean gmLocal) throws SQLException {
        if (curvaTransferencia == null
                || curvaTransferencia.configuracao() == null
                || curvaTransferencia.configuracao().getTipoCurvaFet()
                        != TipoCurvaFet.TRANSFERENCIA) {
            throw new IllegalArgumentException(
                    "Selecione uma curva de transferência configurada.");
        }
        validarFinito(vgsReferencia, "V_GS de referência");
        if (curvas == null) {
            throw new IllegalArgumentException("A lista de curvas não pode ser nula.");
        }

        CurvaDisponivel curvaSaida = curvas.stream()
                .filter(item -> item != null
                        && item.configuracao() != null
                        && item.configuracao().getTipoCurvaFet() == TipoCurvaFet.SAIDA
                        && aproximadamenteIguais(
                                item.configuracao().getValorTensaoConstante(),
                                vgsReferencia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não há curva de saída configurada com o V_GS de referência."));

        double gm;
        ResultadoTransferencia resultadoTransferencia = null;
        ResultadoGmLocal resultadoLocal = null;
        List<PontoFet> pontosTransferencia = carregarPontos(curvaTransferencia.curva());
        if (gmLocal) {
            resultadoLocal = calcularTranscondutanciaLocal(pontosTransferencia).stream()
                    .filter(item -> aproximadamenteIguais(item.vgs(), vgsReferencia))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "O V_GS de referência não possui g_m local calculável."));
            gm = resultadoLocal.transcondutancia();
        } else {
            validarIntervalo(gmMinimo, gmMaximo, "transcondutância");
            if (vgsReferencia < gmMinimo || vgsReferencia > gmMaximo) {
                throw new IllegalArgumentException(
                        "O V_GS de referência deve pertencer ao intervalo de g_m.");
            }
            resultadoTransferencia = analisarTransferenciaPorIntervalo(
                    pontosTransferencia, gmMinimo, gmMaximo);
            gm = resultadoTransferencia.transcondutancia();
        }

        ResultadoSaida resultadoSaida = analisarSaida(
                carregarPontos(curvaSaida.curva()),
                vdsMinimoSaturacao, vdsMaximoSaturacao,
                vdsMinimoSaturacao, vdsMaximoSaturacao);
        ResultadoGanhoIntrinseco ganho = calcularGanhoIntrinseco(
                gm,
                vgsReferencia,
                curvaTransferencia.configuracao().getValorTensaoConstante(),
                resultadoSaida.resistenciaSaida(),
                curvaSaida.configuracao().getValorTensaoConstante(),
                vdsMinimoSaturacao,
                vdsMaximoSaturacao,
                gmLocal);
        return new ResultadoGanhoIntegrado(
                curvaTransferencia, curvaSaida, vgsReferencia,
                resultadoTransferencia, resultadoLocal, resultadoSaida, ganho);
    }

    private boolean aproximadamenteIguais(double primeiro, double segundo) {
        double escala = Math.max(Math.abs(primeiro), Math.abs(segundo));
        double tolerancia = Math.max(TOLERANCIA_ABSOLUTA,
                TOLERANCIA_RELATIVA * escala);
        return Math.abs(primeiro - segundo) <= tolerancia;
    }

    private List<PontoFet> prepararPontos(List<PontoFet> pontos) {
        if (pontos == null) {
            throw new IllegalArgumentException("A lista de pontos não pode ser nula.");
        }
        return pontos.stream()
                .filter(ponto -> ponto != null
                        && Double.isFinite(ponto.tensao())
                        && Double.isFinite(ponto.corrente()))
                .sorted(Comparator.comparingDouble(PontoFet::tensao))
                .toList();
    }

    private List<PontoFet> selecionarIntervalo(List<PontoFet> pontos,
                                                double minimo, double maximo) {
        return pontos.stream()
                .filter(ponto -> ponto.tensao() >= minimo
                        && ponto.tensao() <= maximo)
                .toList();
    }

    private AjusteLinear ajustar(List<PontoFet> pontos, String nomeRegiao) {
        if (pontos.size() < MINIMO_PONTOS) {
            throw new IllegalArgumentException("São necessários pelo menos três pontos na "
                    + nomeRegiao + ".");
        }

        double mediaX = pontos.stream().mapToDouble(PontoFet::tensao).average()
                .orElseThrow();
        double mediaY = pontos.stream().mapToDouble(PontoFet::corrente).average()
                .orElseThrow();
        double somaXX = 0.0;
        double somaXY = 0.0;
        double somaYY = 0.0;

        for (PontoFet ponto : pontos) {
            double dx = ponto.tensao() - mediaX;
            double dy = ponto.corrente() - mediaY;
            somaXX += dx * dx;
            somaXY += dx * dy;
            somaYY += dy * dy;
        }
        if (somaXX == 0.0) {
            throw new IllegalArgumentException(
                    "A variável independente não apresenta variação na " + nomeRegiao + ".");
        }

        double inclinacao = somaXY / somaXX;
        double intercepto = mediaY - inclinacao * mediaX;
        double somaResiduos = 0.0;
        for (PontoFet ponto : pontos) {
            double previsto = inclinacao * ponto.tensao() + intercepto;
            double residuo = ponto.corrente() - previsto;
            somaResiduos += residuo * residuo;
        }
        Double rQuadrado = somaYY == 0.0 ? null : 1.0 - somaResiduos / somaYY;
        return new AjusteLinear(inclinacao, intercepto, rQuadrado, pontos.size());
    }

    private void adicionarAdvertenciasAjuste(List<String> advertencias,
                                              AjusteLinear ajuste,
                                              String nomeRegiao) {
        if (ajuste.rQuadrado() == null) {
            advertencias.add("R² não definido na " + nomeRegiao
                    + ": todos os valores de corrente são iguais.");
        } else if (ajuste.rQuadrado() < LIMITE_R_QUADRADO) {
            advertencias.add("R² inferior a 0,95 na " + nomeRegiao + ".");
        }
        if (ajuste.inclinacao() < 0.0) {
            advertencias.add("A inclinação da " + nomeRegiao + " é negativa.");
        }
    }

    private Double inversoDaInclinacao(double inclinacao, String nome,
                                        List<String> advertencias) {
        if (inclinacao == 0.0) {
            advertencias.add(nome + " é teoricamente infinita: inclinação nula.");
            return Double.POSITIVE_INFINITY;
        }
        return 1.0 / inclinacao;
    }

    private PontoFet calcularJoelho(AjusteLinear ohmico, AjusteLinear saturacao,
                                    List<PontoFet> pontos,
                                    List<String> advertencias) {
        double diferenca = ohmico.inclinacao() - saturacao.inclinacao();
        double escala = Math.max(Math.abs(ohmico.inclinacao()),
                Math.abs(saturacao.inclinacao()));
        double tolerancia = Math.max(TOLERANCIA_ABSOLUTA,
                TOLERANCIA_RELATIVA * escala);
        if (Math.abs(diferenca) <= tolerancia) {
            advertencias.add("Não foi possível calcular o joelho: "
                    + "as inclinações são iguais ou muito próximas.");
            return null;
        }

        double tensao = (saturacao.intercepto() - ohmico.intercepto()) / diferenca;
        double corrente = ohmico.inclinacao() * tensao + ohmico.intercepto();
        if (!Double.isFinite(tensao) || !Double.isFinite(corrente)) {
            advertencias.add("A interseção dos ajustes não é numericamente finita.");
            return null;
        }

        double minimoMedido = pontos.get(0).tensao();
        double maximoMedido = pontos.get(pontos.size() - 1).tensao();
        if (tensao < minimoMedido || tensao > maximoMedido) {
            advertencias.add("A interseção dos ajustes está fora da faixa medida.");
            return null;
        }
        return new PontoFet(tensao, corrente);
    }

    private void validarIntervalo(double minimo, double maximo, String nome) {
        validarFinito(minimo, "limite mínimo da " + nome);
        validarFinito(maximo, "limite máximo da " + nome);
        if (minimo > maximo) {
            throw new IllegalArgumentException("O intervalo da " + nome + " é inválido.");
        }
    }

    private void validarFinito(double valor, String nome) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(nome + " deve ser um número finito.");
        }
    }

    public record PontoFet(double tensao, double corrente) { }

    public record CurvaDisponivel(Curva curva, Coluna colunaX, Coluna colunaY,
                                  CurvaFet configuracao, int quantidadePontos) { }

    public record AjusteLinear(double inclinacao, double intercepto,
                               Double rQuadrado, int quantidadePontos) { }

    public record ResultadoSaida(int pontosValidos, int pontosOhmicos,
                                 int pontosSaturacao,
                                 double tensaoMinimaOhmica,
                                 double tensaoMaximaOhmica,
                                 double tensaoMinimaSaturacao,
                                 double tensaoMaximaSaturacao,
                                 AjusteLinear ajusteOhmico,
                                 AjusteLinear ajusteSaturacao,
                                 double condutanciaOhmica,
                                 Double resistenciaOhmica,
                                 double condutanciaSaida,
                                 Double resistenciaSaida,
                                 PontoFet joelho,
                                 List<String> advertencias) { }

    public record ResultadoTransferencia(int pontosValidos,
                                         int pontosSelecionados,
                                         double tensaoMinima,
                                         double tensaoMaxima,
                                         double transcondutancia,
                                         AjusteLinear ajuste,
                                         List<String> advertencias) { }

    public record ResultadoCurvaSaida(CurvaDisponivel curva,
                                      ResultadoSaida resultado) { }

    public record ParametrosCurvaSaida(
            CurvaDisponivel curva,
            double tensaoMinimaOhmica,
            double tensaoMaximaOhmica,
            double tensaoMinimaSaturacao,
            double tensaoMaximaSaturacao) { }

    public record ResultadoGmLocal(double vgs, double transcondutancia,
                                   AjusteLinear ajuste,
                                   List<String> advertencias) { }

    public record ResultadoGanhoIntrinseco(double ganho,
                                           double ganhoDecibeis,
                                           String classificacao) { }

    public record ResultadoGanhoIntegrado(
            CurvaDisponivel curvaTransferencia,
            CurvaDisponivel curvaSaida,
            double vgsReferencia,
            ResultadoTransferencia resultadoTransferencia,
            ResultadoGmLocal resultadoGmLocal,
            ResultadoSaida resultadoSaida,
            ResultadoGanhoIntrinseco ganho) { }
}
