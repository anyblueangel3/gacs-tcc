package br.uel.gacs.controller;

import br.uel.gacs.application.Autorizador;
import br.uel.gacs.application.SessaoUsuario;
import br.uel.gacs.dao.ColunaDAO;
import br.uel.gacs.dao.ConexaoBanco;
import br.uel.gacs.dao.CurvaDAO;
import br.uel.gacs.dao.CurvaGraficoDAO;
import br.uel.gacs.dao.DadoColunaDAO;
import br.uel.gacs.dao.GraficoDAO;
import br.uel.gacs.dao.ExperimentoDAO;
import br.uel.gacs.model.Coluna;
import br.uel.gacs.model.Curva;
import br.uel.gacs.model.CurvaGrafico;
import br.uel.gacs.model.DadoColuna;
import br.uel.gacs.model.Grafico;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Valida e coordena a criação e manutenção de gráficos e de suas curvas.
 *
 * @author Ronaldo Rodrigues Godoi e Chat GPT
 */
public final class GraficoCtlr {
    private final ColunaDAO colunaDAO;
    private final CurvaDAO curvaDAO;
    private final GraficoDAO graficoDAO;
    private final CurvaGraficoDAO curvaGraficoDAO;
    private final ExperimentoDAO experimentoDAO;
    private final DadoColunaDAO dadoColunaDAO;

    public GraficoCtlr() {
        this(new ColunaDAO(), new CurvaDAO(), new GraficoDAO(), new CurvaGraficoDAO(),
                new ExperimentoDAO(), new DadoColunaDAO());
    }

    GraficoCtlr(ColunaDAO colunaDAO, CurvaDAO curvaDAO, GraficoDAO graficoDAO,
                CurvaGraficoDAO curvaGraficoDAO, ExperimentoDAO experimentoDAO,
                DadoColunaDAO dadoColunaDAO) {
        if (colunaDAO == null || curvaDAO == null || graficoDAO == null || curvaGraficoDAO == null
                || experimentoDAO == null || dadoColunaDAO == null) {
            throw new IllegalArgumentException("Os DAOs de gráfico devem ser informados.");
        }
        this.colunaDAO = colunaDAO;
        this.curvaDAO = curvaDAO;
        this.graficoDAO = graficoDAO;
        this.curvaGraficoDAO = curvaGraficoDAO;
        this.experimentoDAO = experimentoDAO;
        this.dadoColunaDAO = dadoColunaDAO;
    }

    public List<Coluna> listarColunas(Long idExperimento) throws SQLException {
        if (idExperimento == null) throw new IllegalArgumentException("Salve o experimento antes de criar um gráfico.");
        return colunaDAO.listarPorExperimento(idExperimento);
    }

    public List<Grafico> listarGraficos(Long idExperimento) throws SQLException {
        if (idExperimento == null) throw new IllegalArgumentException("O experimento deve estar salvo.");
        return graficoDAO.listarPorExperimento(idExperimento);
    }

    public boolean podeAlterar(Long idExperimento) throws SQLException {
        if (Autorizador.usuarioAtualEhAdministrador()) return true;
        Long usuario = SessaoUsuario.exigirUsuarioLogado().getId();
        return experimentoDAO.buscarPorId(idExperimento)
                .map(e -> usuario.equals(e.getIdUsuario())).orElse(false);
    }

    public List<CurvaDoGrafico> listarCurvas(Grafico grafico) throws SQLException {
        if (grafico == null || grafico.getId() == null) return List.of();
        Map<Long, Coluna> colunas = new java.util.HashMap<>();
        for (Coluna coluna : listarColunas(grafico.getIdExperimento())) colunas.put(coluna.getId(), coluna);
        java.util.ArrayList<CurvaDoGrafico> resultado = new java.util.ArrayList<>();
        for (CurvaGrafico item : curvaGraficoDAO.listarPorGrafico(grafico.getId())) {
            Curva curva = curvaDAO.buscarPorId(item.getIdCurva())
                    .orElseThrow(() -> new SQLException("Uma curva associada não foi encontrada."));
            resultado.add(new CurvaDoGrafico(item.getNumeroCurva(), curva,
                    colunas.get(curva.getIdColunaX()), colunas.get(curva.getIdColunaY())));
        }
        return List.copyOf(resultado);
    }

    /** Carrega os pontos de todas as curvas, associando X e Y pelo número da medida. */
    public List<SerieDoGrafico> carregarSeries(Grafico grafico) throws SQLException {
        if (grafico == null || grafico.getId() == null) {
            throw new IllegalArgumentException("Selecione um gráfico para plotar.");
        }
        java.util.ArrayList<SerieDoGrafico> series = new java.util.ArrayList<>();
        for (CurvaDoGrafico item : listarCurvas(grafico)) {
            if (item.colunaX() == null || item.colunaY() == null) {
                throw new SQLException("Uma das colunas da curva não foi encontrada.");
            }
            Map<Integer, Double> valoresX = new java.util.HashMap<>();
            for (DadoColuna dado : dadoColunaDAO.listarPorColuna(item.colunaX().getId())) {
                valoresX.put(dado.getNumeroDaMedida(), dado.getValorMedida());
            }
            java.util.ArrayList<PontoGrafico> pontos = new java.util.ArrayList<>();
            for (DadoColuna dadoY : dadoColunaDAO.listarPorColuna(item.colunaY().getId())) {
                Double x = valoresX.get(dadoY.getNumeroDaMedida());
                if (x != null && Double.isFinite(x) && Double.isFinite(dadoY.getValorMedida())) {
                    pontos.add(new PontoGrafico(x, dadoY.getValorMedida()));
                }
            }
            series.add(new SerieDoGrafico(item.numero(), item.curva().getNome(),
                    item.colunaX().getNomeColuna(), item.colunaY().getNomeColuna(), List.copyOf(pontos)));
        }
        return List.copyOf(series);
    }

    /** Cria o gráfico, sua primeira curva e a associação entre ambos atomicamente. */
    public Grafico criarComPrimeiraCurva(Long idExperimento, String nomeGrafico,
                                         String nomeCurva, Coluna colunaX, Coluna colunaY)
            throws SQLException {
        validar(idExperimento, nomeGrafico, nomeCurva, colunaX, colunaY);
        exigirPermissao(idExperimento);
        Grafico grafico = new Grafico(null, idExperimento, nomeGrafico.trim());
        Curva curva = new Curva(null, nomeCurva.trim(), colunaX.getId(), colunaY.getId());

        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            boolean autoCommit = conexao.getAutoCommit();
            conexao.setAutoCommit(false);
            try {
                validarColunasPersistidas(conexao, idExperimento, colunaX, colunaY);
                curvaDAO.inserir(conexao, curva);
                graficoDAO.inserir(conexao, grafico);
                curvaGraficoDAO.inserir(conexao, new CurvaGrafico(grafico.getId(), 1, curva.getId()));
                conexao.commit();
                return grafico;
            } catch (SQLException | RuntimeException e) {
                try { conexao.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            } finally {
                try { conexao.setAutoCommit(autoCommit); } catch (SQLException ignored) { }
            }
        }
    }

    public void renomearGrafico(Grafico grafico, String novoNome) throws SQLException {
        if (grafico == null || grafico.getId() == null) throw new IllegalArgumentException("Selecione um gráfico.");
        validarNome(novoNome, "gráfico");
        exigirPermissao(grafico.getIdExperimento());
        String anterior = grafico.getNome(); grafico.setNome(novoNome.trim());
        try { if (!graficoDAO.atualizar(grafico)) throw new SQLException("O gráfico não foi encontrado."); }
        catch (SQLException | RuntimeException e) { grafico.setNome(anterior); throw e; }
    }

    public void adicionarCurva(Grafico grafico, String nome, Coluna x, Coluna y) throws SQLException {
        if (grafico == null || grafico.getId() == null) throw new IllegalArgumentException("Selecione um gráfico.");
        validar(grafico.getIdExperimento(), grafico.getNome(), nome, x, y);
        exigirPermissao(grafico.getIdExperimento());
        Curva curva = new Curva(null, nome.trim(), x.getId(), y.getId());
        emTransacao(conexao -> {
            validarColunasPersistidas(conexao, grafico.getIdExperimento(), x, y);
            int numero = curvaGraficoDAO.listarPorGrafico(conexao, grafico.getId()).size() + 1;
            curvaDAO.inserir(conexao, curva);
            curvaGraficoDAO.inserir(conexao, new CurvaGrafico(grafico.getId(), numero, curva.getId()));
        });
    }

    public void alterarCurva(Grafico grafico, Curva curva, String nome, Coluna x, Coluna y)
            throws SQLException {
        if (grafico == null || curva == null || curva.getId() == null) throw new IllegalArgumentException("Selecione uma curva.");
        validar(grafico.getIdExperimento(), grafico.getNome(), nome, x, y);
        exigirPermissao(grafico.getIdExperimento());
        emTransacao(conexao -> {
            validarColunasPersistidas(conexao, grafico.getIdExperimento(), x, y);
            Curva alterada = new Curva(curva.getId(), nome.trim(), x.getId(), y.getId());
            if (!curvaDAO.atualizar(conexao, alterada)) throw new SQLException("A curva não foi encontrada.");
        });
    }

    public void removerCurva(Grafico grafico, CurvaDoGrafico selecionada) throws SQLException {
        if (grafico == null || selecionada == null) throw new IllegalArgumentException("Selecione uma curva.");
        exigirPermissao(grafico.getIdExperimento());
        emTransacao(conexao -> {
            List<CurvaGrafico> itens = curvaGraficoDAO.listarPorGrafico(conexao, grafico.getId());
            if (itens.size() <= 1) throw new IllegalArgumentException("Um gráfico deve possuir ao menos uma curva. Exclua o gráfico completo.");
            curvaGraficoDAO.excluirPorGrafico(conexao, grafico.getId());
            List<CurvaGrafico> restantes = itens.stream()
                    .filter(i -> !i.getIdCurva().equals(selecionada.curva().getId())).toList();
            for (int i=0;i<restantes.size();i++) curvaGraficoDAO.inserir(conexao,
                    new CurvaGrafico(grafico.getId(), i+1, restantes.get(i).getIdCurva()));
            if (curvaGraficoDAO.contarReferenciasDaCurva(conexao, selecionada.curva().getId()) == 0)
                curvaDAO.excluir(conexao, selecionada.curva().getId());
        });
    }

    public void excluirGrafico(Grafico grafico) throws SQLException {
        if (grafico == null || grafico.getId() == null) throw new IllegalArgumentException("Selecione um gráfico.");
        exigirPermissao(grafico.getIdExperimento());
        emTransacao(conexao -> {
            List<CurvaGrafico> itens = curvaGraficoDAO.listarPorGrafico(conexao, grafico.getId());
            curvaGraficoDAO.excluirPorGrafico(conexao, grafico.getId());
            if (!graficoDAO.excluir(conexao, grafico.getId())) throw new SQLException("O gráfico não foi encontrado.");
            for (CurvaGrafico item : itens)
                if (curvaGraficoDAO.contarReferenciasDaCurva(conexao, item.getIdCurva()) == 0)
                    curvaDAO.excluir(conexao, item.getIdCurva());
        });
    }

    private void validar(Long idExperimento, String nomeGrafico, String nomeCurva,
                         Coluna colunaX, Coluna colunaY) {
        if (idExperimento == null) throw new IllegalArgumentException("Salve o experimento antes de criar um gráfico.");
        validarNome(nomeGrafico, "gráfico");
        validarNome(nomeCurva, "curva");
        if (colunaX == null || colunaY == null) throw new IllegalArgumentException("Selecione as colunas X e Y.");
        if (colunaX.getId() == null || colunaY.getId() == null) throw new IllegalArgumentException("As colunas devem estar salvas.");
        if (colunaX.getId().equals(colunaY.getId())) throw new IllegalArgumentException("As colunas X e Y devem ser diferentes.");
        if (!idExperimento.equals(colunaX.getIdExperimento()) || !idExperimento.equals(colunaY.getIdExperimento())) {
            throw new IllegalArgumentException("As colunas X e Y devem pertencer ao experimento aberto.");
        }
    }

    private void validarColunasPersistidas(Connection conexao, Long idExperimento,
                                            Coluna colunaX, Coluna colunaY) throws SQLException {
        Coluna x = colunaDAO.buscarPorId(conexao, colunaX.getId())
                .orElseThrow(() -> new IllegalArgumentException("A coluna X não foi encontrada."));
        Coluna y = colunaDAO.buscarPorId(conexao, colunaY.getId())
                .orElseThrow(() -> new IllegalArgumentException("A coluna Y não foi encontrada."));
        if (!idExperimento.equals(x.getIdExperimento()) || !idExperimento.equals(y.getIdExperimento())) {
            throw new IllegalArgumentException("As colunas selecionadas não pertencem ao experimento aberto.");
        }
    }

    private void validarNome(String nome, String entidade) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Informe o nome do " + entidade + ".");
        if (nome.trim().length() > 200) throw new IllegalArgumentException("O nome do " + entidade + " deve possuir no máximo 200 caracteres.");
    }

    private void exigirPermissao(Long idExperimento) throws SQLException {
        if (!podeAlterar(idExperimento))
            throw new SecurityException("Somente o proprietário do experimento ou um administrador pode alterar seus gráficos.");
    }

    private void emTransacao(Operacao operacao) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            boolean auto = conexao.getAutoCommit(); conexao.setAutoCommit(false);
            try { operacao.executar(conexao); conexao.commit(); }
            catch (SQLException | RuntimeException e) {
                try { conexao.rollback(); } catch (SQLException r) { e.addSuppressed(r); }
                throw e;
            } finally { try { conexao.setAutoCommit(auto); } catch (SQLException ignored) { } }
        }
    }

    public record CurvaDoGrafico(Integer numero, Curva curva, Coluna colunaX, Coluna colunaY) { }
    public record PontoGrafico(double x, double y) { }
    public record SerieDoGrafico(Integer numero, String nome, String nomeEixoX,
                                 String nomeEixoY, List<PontoGrafico> pontos) { }
    @FunctionalInterface private interface Operacao { void executar(Connection conexao) throws SQLException; }
}
