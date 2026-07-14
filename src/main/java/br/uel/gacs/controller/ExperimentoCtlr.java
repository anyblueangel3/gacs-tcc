package br.uel.gacs.controller;

import br.uel.gacs.application.PlanilhaExperimento;
import br.uel.gacs.dao.ColunaDAO;
import br.uel.gacs.dao.ConexaoBanco;
import br.uel.gacs.dao.DadoColunaDAO;
import br.uel.gacs.dao.ExperimentoDAO;
import br.uel.gacs.dao.UsuarioDAO;
import br.uel.gacs.dao.CurvaDAO;
import br.uel.gacs.dao.CurvaGraficoDAO;
import br.uel.gacs.dao.GraficoDAO;
import br.uel.gacs.model.Coluna;
import br.uel.gacs.model.DadoColuna;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.Usuario;
import br.uel.gacs.model.Curva;
import br.uel.gacs.model.Grafico;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Valida e coordena as operações iniciais de experimentos. */
public final class ExperimentoCtlr {
    private final ExperimentoDAO experimentoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ColunaDAO colunaDAO;
    private final DadoColunaDAO dadoColunaDAO;
    private final CurvaDAO curvaDAO = new CurvaDAO();
    private final CurvaGraficoDAO curvaGraficoDAO = new CurvaGraficoDAO();
    private final GraficoDAO graficoDAO = new GraficoDAO();

    public ExperimentoCtlr() {
        this(new ExperimentoDAO(), new UsuarioDAO(), new ColunaDAO(), new DadoColunaDAO());
    }

    public ExperimentoCtlr(ExperimentoDAO experimentoDAO, UsuarioDAO usuarioDAO) {
        this(experimentoDAO, usuarioDAO, new ColunaDAO(), new DadoColunaDAO());
    }

    public ExperimentoCtlr(ExperimentoDAO experimentoDAO, UsuarioDAO usuarioDAO,
                           ColunaDAO colunaDAO, DadoColunaDAO dadoColunaDAO) {
        if (experimentoDAO == null || usuarioDAO == null || colunaDAO == null || dadoColunaDAO == null) {
            throw new IllegalArgumentException("Os DAOs de experimento e usuário devem ser informados.");
        }
        this.experimentoDAO = experimentoDAO;
        this.usuarioDAO = usuarioDAO;
        this.colunaDAO = colunaDAO;
        this.dadoColunaDAO = dadoColunaDAO;
    }

    public Experimento criarNovo(Long idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("O usuário responsável deve ser informado.");
        }
        return new Experimento(null, "Experimento sem título", LocalDateTime.now(), "", idUsuario);
    }

    public Long salvarNovo(Experimento experimento) throws SQLException {
        validar(experimento);
        if (experimento.getId() != null) {
            throw new IllegalArgumentException("O experimento informado já foi salvo.");
        }
        return experimentoDAO.inserir(experimento);
    }

    public void salvarAlteracoes(Experimento experimento) throws SQLException {
        validar(experimento);
        if (experimento.getId() == null) {
            throw new IllegalArgumentException("O experimento ainda não foi salvo.");
        }
        if (!experimentoDAO.atualizar(experimento)) {
            throw new SQLException("O experimento não foi encontrado para atualização.");
        }
    }

    /** Persiste o experimento e sua planilha em uma única transação. */
    public void salvarCompleto(Experimento experimento, PlanilhaExperimento planilha) throws SQLException {
        validar(experimento);
        validarPlanilha(planilha);
        Long idOriginal = experimento.getId();
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            boolean auto = conexao.getAutoCommit(); conexao.setAutoCommit(false);
            try {
                if (experimento.getId() == null) experimentoDAO.inserir(conexao, experimento);
                else if (!experimentoDAO.atualizar(conexao, experimento)) throw new SQLException("O experimento não foi encontrado.");
                List<Coluna> existentes = colunaDAO.listarPorExperimento(conexao, experimento.getId());
                Map<Short, Coluna> existentesPorRotulo = new HashMap<>();
                for (Coluna coluna : existentes) existentesPorRotulo.put(coluna.getRotulo(), coluna);
                java.util.HashSet<Short> rotulosMantidos = new java.util.HashSet<>();
                for (int coluna=0; coluna<planilha.getQuantidadeColunas(); coluna++) {
                    short rotulo = planilha.getNumeroRotuloColuna(coluna);
                    rotulosMantidos.add(rotulo);
                    Coluna entidade;
                    if (existentesPorRotulo.containsKey(rotulo)) {
                        entidade=existentesPorRotulo.get(rotulo); entidade.setNomeColuna(planilha.getNomeColuna(coluna));
                        if(!colunaDAO.atualizar(conexao,entidade))throw new SQLException("Não foi possível atualizar uma coluna.");
                    } else {
                        entidade=new Coluna(null,experimento.getId(),rotulo,planilha.getNomeColuna(coluna));
                        colunaDAO.inserir(conexao,entidade);
                    }
                    dadoColunaDAO.excluirPorColuna(conexao,entidade.getId());
                    java.util.ArrayList<DadoColuna> dados=new java.util.ArrayList<>();
                    for(int linha=0;linha<planilha.getQuantidadeMedidas();linha++) {
                        Double valor=planilha.getValor(linha,coluna);
                        if(valor!=null)dados.add(new DadoColuna(entidade.getId(),linha+1,valor));
                    }
                    dadoColunaDAO.inserirTodos(conexao,dados);
                }
                for (Coluna removida : existentes) if (!rotulosMantidos.contains(removida.getRotulo()))
                    excluirColunaPersistida(conexao, removida.getId());
                conexao.commit();
            } catch(SQLException|RuntimeException e) {
                if(idOriginal==null)experimento.setId(null);
                try{conexao.rollback();}catch(SQLException r){e.addSuppressed(r);} throw e;
            } finally { try{conexao.setAutoCommit(auto);}catch(SQLException ignored){} }
        }
    }

    public PlanilhaExperimento carregarPlanilha(Long idExperimento) throws SQLException {
        PlanilhaExperimento planilha=new PlanilhaExperimento();
        List<Coluna> colunas=colunaDAO.listarPorExperimento(idExperimento);
        for(Coluna coluna:colunas)planilha.adicionarColuna(coluna.getNomeColuna(), coluna.getRotulo());
        if(colunas.isEmpty())return planilha;
        java.util.ArrayList<List<DadoColuna>> dados=new java.util.ArrayList<>();
        int quantidadeLinhas=0;
        for(Coluna coluna:colunas){
            List<DadoColuna> valores=dadoColunaDAO.listarPorColuna(coluna.getId()); dados.add(valores);
            for(DadoColuna valor:valores)quantidadeLinhas=Math.max(quantidadeLinhas,valor.getNumeroDaMedida());
        }
        for(int i=0;i<quantidadeLinhas;i++)planilha.adicionarMedidaVazia();
        for(int coluna=0;coluna<dados.size();coluna++)for(DadoColuna valor:dados.get(coluna))
            planilha.setValor(valor.getNumeroDaMedida()-1,coluna,valor.getValorMedida());
        return planilha;
    }

    private void validarPlanilha(PlanilhaExperimento planilha) {
        if(planilha==null||planilha.getQuantidadeColunas()==0)throw new IllegalArgumentException("Adicione ao menos uma coluna ao experimento.");
        for(int i=0;i<planilha.getQuantidadeColunas();i++)if(planilha.getNomeColuna(i).isBlank())
            throw new IllegalArgumentException("Informe o nome da coluna "+planilha.getRotuloColuna(i)+".");
    }

    public Optional<Experimento> buscarPorId(Long id) throws SQLException {
        return experimentoDAO.buscarPorId(id);
    }

    public List<ExperimentoListado> listarVisiveis(Usuario usuario, boolean administrador)
            throws SQLException {
        List<Experimento> experimentos = administrador
                ? experimentoDAO.listarTodos()
                : experimentoDAO.listarPorUsuario(usuario.getId());
        Map<Long, String> proprietarios = new HashMap<>();
        if (administrador) {
            for (Usuario item : usuarioDAO.listarTodos()) proprietarios.put(item.getId(), item.getNome());
        } else {
            proprietarios.put(usuario.getId(), usuario.getNome());
        }
        return experimentos.stream()
                .map(e -> new ExperimentoListado(e, proprietarios.getOrDefault(e.getIdUsuario(), "Usuário não encontrado")))
                .toList();
    }

    public record ExperimentoListado(Experimento experimento, String proprietario) { }

    public void excluirExperimento(Experimento experimento, Usuario usuario, boolean administrador) throws SQLException {
        exigirPermissao(experimento, usuario, administrador);
        if (experimento.getId() == null) return;
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            boolean auto = conexao.getAutoCommit(); conexao.setAutoCommit(false);
            try {
                List<Grafico> graficos = graficoDAO.listarPorExperimento(conexao, experimento.getId());
                for (Grafico grafico : graficos)
                    curvaGraficoDAO.excluirPorGrafico(conexao, grafico.getId());
                for (Curva curva : curvaDAO.listarPorExperimento(conexao, experimento.getId()))
                    curvaDAO.excluir(conexao, curva.getId());
                for (Grafico grafico : graficos)
                    graficoDAO.excluir(conexao, grafico.getId());
                for (Coluna coluna : colunaDAO.listarPorExperimento(conexao, experimento.getId())) {
                    dadoColunaDAO.excluirPorColuna(conexao, coluna.getId());
                    colunaDAO.excluir(conexao, coluna.getId());
                }
                if (!experimentoDAO.excluir(conexao, experimento.getId())) throw new SQLException("O experimento não foi encontrado.");
                conexao.commit();
            } catch (SQLException | RuntimeException e) { try { conexao.rollback(); } catch (SQLException r) { e.addSuppressed(r); } throw e; }
            finally { try { conexao.setAutoCommit(auto); } catch (SQLException ignored) { } }
        }
    }

    private void excluirColunaPersistida(Connection conexao, Long idColuna) throws SQLException {
        for (Curva curva : curvaDAO.listarPorColuna(conexao, idColuna)) {
            curvaGraficoDAO.excluirPorCurva(conexao, curva.getId());
            curvaDAO.excluir(conexao, curva.getId());
        }
        dadoColunaDAO.excluirPorColuna(conexao, idColuna);
        if (!colunaDAO.excluir(conexao, idColuna)) throw new SQLException("A coluna não foi encontrada.");
    }

    private void exigirPermissao(Experimento experimento, Usuario usuario, boolean administrador) {
        if (experimento == null || usuario == null || (!administrador && !usuario.getId().equals(experimento.getIdUsuario())))
            throw new SecurityException("Somente o proprietário do experimento ou um administrador pode excluí-lo.");
    }

    private void validar(Experimento experimento) {
        if (experimento == null) throw new IllegalArgumentException("O experimento deve ser informado.");
        if (experimento.getNomeExperimento() == null || experimento.getNomeExperimento().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do experimento.");
        }
        if (experimento.getNomeExperimento().trim().length() > 200) {
            throw new IllegalArgumentException("O nome do experimento deve possuir no máximo 200 caracteres.");
        }
        if (experimento.getDataExperimento() == null) throw new IllegalArgumentException("Informe a data do experimento.");
        if (experimento.getIdUsuario() == null) throw new IllegalArgumentException("O responsável pelo experimento não foi identificado.");
    }
}
