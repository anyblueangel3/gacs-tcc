package br.uel.gacs.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Gera e verifica hashes seguros de senha usando PBKDF2. */
public final class SenhaUtil {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 210_000;
    private static final int TAMANHO_SAL_BYTES = 16;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final String SEPARADOR = ":";
    private static final SecureRandom GERADOR_ALEATORIO = new SecureRandom();

    private SenhaUtil() { }

    /** Gera um hash contendo algoritmo, iterações, sal e resultado. */
    public static String gerarHash(String senha) {
        validarSenhaPreenchida(senha);

        byte[] sal = new byte[TAMANHO_SAL_BYTES];
        GERADOR_ALEATORIO.nextBytes(sal);
        byte[] hash = calcularHash(senha.toCharArray(), sal, ITERACOES);

        return String.join(
                SEPARADOR,
                ALGORITMO,
                Integer.toString(ITERACOES),
                Base64.getEncoder().encodeToString(sal),
                Base64.getEncoder().encodeToString(hash));
    }

    /** Verifica uma senha comum em relação ao hash armazenado. */
    public static boolean verificarSenha(String senha, String hashArmazenado) {
        if (senha == null || senha.isEmpty()
                || hashArmazenado == null || hashArmazenado.isBlank()) {
            return false;
        }

        try {
            String[] partes = hashArmazenado.split(SEPARADOR, -1);

            if (partes.length != 4 || !ALGORITMO.equals(partes[0])) {
                return false;
            }

            int iteracoes = Integer.parseInt(partes[1]);
            if (iteracoes <= 0) {
                return false;
            }

            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
            byte[] hashCalculado = calcularHash(senha.toCharArray(), sal, iteracoes);

            return MessageDigest.isEqual(hashEsperado, hashCalculado);
        } catch (IllegalArgumentException excecao) {
            return false;
        }
    }

    private static byte[] calcularHash(char[] senha, byte[] sal, int iteracoes) {
        PBEKeySpec especificacao = new PBEKeySpec(
                senha, sal, iteracoes, TAMANHO_HASH_BITS);

        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance(ALGORITMO);
            return fabrica.generateSecret(especificacao).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException excecao) {
            throw new IllegalStateException("Não foi possível gerar o hash da senha.", excecao);
        } finally {
            especificacao.clearPassword();
        }
    }

    private static void validarSenhaPreenchida(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha deve ser preenchida.");
        }
    }
}
