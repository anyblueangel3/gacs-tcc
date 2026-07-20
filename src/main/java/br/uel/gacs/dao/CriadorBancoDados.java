package br.uel.gacs.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/** Cria o banco DadosGACS e suas tabelas na primeira execução do sistema. */
public final class CriadorBancoDados {
    private static final String[] COMANDOS_CRIACAO_TABELAS = {
        """
        CREATE TABLE Usuario (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            nome VARCHAR(150) NOT NULL,
            email VARCHAR(254) NOT NULL UNIQUE,
            senhaHash VARCHAR(255) NOT NULL,
            perfil VARCHAR(20) NOT NULL,
            ativo BOOLEAN NOT NULL DEFAULT TRUE,
            dataCriacao DATETIME NOT NULL,
            dataUltimaAlteracao DATETIME NOT NULL
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE Experimento (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            nomeExperimento VARCHAR(200) NOT NULL,
            dataExperimento DATETIME NOT NULL,
            observacoes TEXT,
            idUsuario BIGINT NOT NULL,
            CONSTRAINT fk_experimento_usuario
                FOREIGN KEY (idUsuario) REFERENCES Usuario(id)
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE Coluna (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            idExperimento BIGINT NOT NULL,
            rotulo SMALLINT,
            nomeColuna VARCHAR(200),
            CONSTRAINT uq_coluna_experimento_rotulo
                UNIQUE (idExperimento, rotulo),
            CONSTRAINT ck_coluna_rotulo
                CHECK (rotulo BETWEEN 1 AND 50),
            CONSTRAINT fk_coluna_experimento
                FOREIGN KEY (idExperimento) REFERENCES Experimento(id)
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE DadoColuna (
            idColuna BIGINT NOT NULL,
            numeroDaMedida INT NOT NULL,
            valorMedida DOUBLE NOT NULL,
            PRIMARY KEY (idColuna, numeroDaMedida),
            CONSTRAINT ck_dado_coluna_numero_medida
                CHECK (numeroDaMedida BETWEEN 1 AND 10000),
            CONSTRAINT fk_dado_coluna_coluna
                FOREIGN KEY (idColuna) REFERENCES Coluna(id)
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE Curva (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            nome VARCHAR(200) NOT NULL,
            idColunaX BIGINT NOT NULL,
            idColunaY BIGINT NOT NULL,
            CONSTRAINT ck_curva_colunas_diferentes
                CHECK (idColunaX <> idColunaY),
            CONSTRAINT fk_curva_coluna_x
                FOREIGN KEY (idColunaX) REFERENCES Coluna(id),
            CONSTRAINT fk_curva_coluna_y
                FOREIGN KEY (idColunaY) REFERENCES Coluna(id)
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE CurvaFet (
            idCurva BIGINT PRIMARY KEY,
            tipoCurvaFet VARCHAR(20) NOT NULL,
            valorTensaoConstante DOUBLE NOT NULL,
            CONSTRAINT ck_curva_fet_tipo
                CHECK (tipoCurvaFet IN ('SAIDA', 'TRANSFERENCIA')),
            CONSTRAINT fk_curva_fet_curva
                FOREIGN KEY (idCurva) REFERENCES Curva(id)
                ON DELETE CASCADE
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE Grafico (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            idExperimento BIGINT NOT NULL,
            nome VARCHAR(200) NOT NULL,
            CONSTRAINT fk_grafico_experimento
                FOREIGN KEY (idExperimento) REFERENCES Experimento(id)
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE CurvaGrafico (
            idGrafico BIGINT NOT NULL,
            numeroCurva INT NOT NULL,
            idCurva BIGINT NOT NULL,
            PRIMARY KEY (idGrafico, numeroCurva),
            CONSTRAINT uq_curva_grafico
                UNIQUE (idGrafico, idCurva),
            CONSTRAINT ck_curva_grafico_numero
                CHECK (numeroCurva >= 1),
            CONSTRAINT fk_curva_grafico_grafico
                FOREIGN KEY (idGrafico) REFERENCES Grafico(id),
            CONSTRAINT fk_curva_grafico_curva
                FOREIGN KEY (idCurva) REFERENCES Curva(id)
        ) ENGINE=InnoDB
        """
    };

    private CriadorBancoDados() { }

    /**
     * Cria o banco e as tabelas somente quando DadosGACS ainda não existe.
     *
     * @return true quando o banco foi criado nesta execução; false quando já existia
     */
    public static boolean criarSeNaoExistir() throws SQLException {
        if (bancoExiste()) {
            return false;
        }

        criarBanco();
        criarTabelas();
        return true;
    }

    private static boolean bancoExiste() throws SQLException {
        String sql = """
                SELECT SCHEMA_NAME
                FROM INFORMATION_SCHEMA.SCHEMATA
                WHERE SCHEMA_NAME = ?
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoServidor();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, ConexaoBanco.getNomeBanco());

            try (var resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private static void criarBanco() throws SQLException {
        validarNomeBanco();

        String sql = "CREATE DATABASE " + ConexaoBanco.getNomeBanco()
                + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";

        try (Connection conexao = ConexaoBanco.obterConexaoServidor();
             Statement comando = conexao.createStatement()) {
            comando.executeUpdate(sql);
        }
    }

    private static void criarTabelas() throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             Statement comando = conexao.createStatement()) {
            for (String sql : COMANDOS_CRIACAO_TABELAS) {
                comando.executeUpdate(sql);
            }
        }
    }

    private static void validarNomeBanco() {
        String nomeBanco = ConexaoBanco.getNomeBanco();

        if (!nomeBanco.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("O nome configurado para o banco de dados é inválido.");
        }
    }
}
