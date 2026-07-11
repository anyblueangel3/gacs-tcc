package br.uel.gacs.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Fornece conexões JDBC com o MySQL local e com o banco DadosGACS. */
public final class ConexaoBanco {
    private static final String ARQUIVO_CONFIGURACAO = "/database.properties";
    private static final Properties CONFIGURACAO = carregarConfiguracao();

    private ConexaoBanco() { }

    /** Obtém uma conexão com o MySQL local sem selecionar um banco de dados. */
    public static Connection obterConexaoServidor() throws SQLException {
        return DriverManager.getConnection(
                montarUrl(false),
                CONFIGURACAO.getProperty("db.usuario"),
                CONFIGURACAO.getProperty("db.senha"));
    }

    /** Obtém uma conexão com o banco DadosGACS. */
    public static Connection obterConexaoBanco() throws SQLException {
        return DriverManager.getConnection(
                montarUrl(true),
                CONFIGURACAO.getProperty("db.usuario"),
                CONFIGURACAO.getProperty("db.senha"));
    }

    public static String getNomeBanco() {
        return CONFIGURACAO.getProperty("db.nome");
    }

    private static String montarUrl(boolean incluirBanco) {
        StringBuilder url = new StringBuilder("jdbc:mysql://")
                .append(CONFIGURACAO.getProperty("db.host"))
                .append(":")
                .append(CONFIGURACAO.getProperty("db.porta"))
                .append("/");

        if (incluirBanco) {
            url.append(CONFIGURACAO.getProperty("db.nome"));
        }

        return url.append("?useSSL=false")
                .append("&allowPublicKeyRetrieval=true")
                .append("&serverTimezone=America/Sao_Paulo")
                .append("&useUnicode=true")
                .append("&characterEncoding=UTF-8")
                .toString();
    }

    private static Properties carregarConfiguracao() {
        Properties propriedades = new Properties();

        try (InputStream entrada = ConexaoBanco.class.getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "Arquivo database.properties não encontrado em src/main/resources.");
            }

            propriedades.load(entrada);
            validarConfiguracao(propriedades);
            return propriedades;
        } catch (IOException excecao) {
            throw new IllegalStateException("Não foi possível ler database.properties.", excecao);
        }
    }

    private static void validarConfiguracao(Properties propriedades) {
        exigirPreenchida(propriedades, "db.host");
        exigirPreenchida(propriedades, "db.porta");
        exigirPreenchida(propriedades, "db.nome");
        exigirPreenchida(propriedades, "db.usuario");

        if (!propriedades.containsKey("db.senha")) {
            throw new IllegalStateException("A propriedade db.senha não foi definida.");
        }
    }

    private static void exigirPreenchida(Properties propriedades, String chave) {
        String valor = propriedades.getProperty(chave);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("A propriedade " + chave + " não foi preenchida.");
        }
    }
}
