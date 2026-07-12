package br.uel.gacs.controller;

import br.uel.gacs.dao.ExperimentoDAO;
import br.uel.gacs.dao.UsuarioDAO;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.Usuario;

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

    public ExperimentoCtlr() {
        this(new ExperimentoDAO(), new UsuarioDAO());
    }

    public ExperimentoCtlr(ExperimentoDAO experimentoDAO, UsuarioDAO usuarioDAO) {
        if (experimentoDAO == null || usuarioDAO == null) {
            throw new IllegalArgumentException("Os DAOs de experimento e usuário devem ser informados.");
        }
        this.experimentoDAO = experimentoDAO;
        this.usuarioDAO = usuarioDAO;
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
