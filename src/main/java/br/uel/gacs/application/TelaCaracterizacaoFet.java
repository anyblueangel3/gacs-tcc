package br.uel.gacs.application;

import br.uel.gacs.controller.CaracterizacaoFetCtlr;
import br.uel.gacs.controller.CaracterizacaoFetCtlr.CurvaDisponivel;
import br.uel.gacs.controller.CaracterizacaoFetCtlr.ResultadoGmLocal;
import br.uel.gacs.controller.CaracterizacaoFetCtlr.ResultadoGanhoIntegrado;
import br.uel.gacs.controller.CaracterizacaoFetCtlr.ResultadoCurvaSaida;
import br.uel.gacs.controller.CaracterizacaoFetCtlr.ResultadoSaida;
import br.uel.gacs.controller.CaracterizacaoFetCtlr.ResultadoTransferencia;
import br.uel.gacs.model.CurvaFet;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.TipoCurvaFet;
import br.uel.gacs.util.FormatadorNumero;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

/** Configura curvas e apresenta a primeira caracterização genérica de FET. */
public final class TelaCaracterizacaoFet {
    private final Window janelaPai;
    private final Long idExperimento;
    private final CaracterizacaoFetCtlr controller = new CaracterizacaoFetCtlr();

    public TelaCaracterizacaoFet(Window janelaPai, Long idExperimento) {
        this.janelaPai = janelaPai;
        this.idExperimento = idExperimento;
    }

    public void exibir() {
        Experimento experimento;
        List<CurvaDisponivel> curvas;
        try {
            experimento = controller.buscarExperimento(idExperimento);
            curvas = controller.listarCurvas(idExperimento);
        } catch (SQLException erro) {
            erro("Não foi possível carregar as curvas do experimento.");
            return;
        }
        if (curvas.isEmpty()) {
            erro("O experimento não possui curvas cadastradas.");
            return;
        }

        Stage janela = new Stage();
        janela.initOwner(janelaPai);
        janela.initModality(Modality.WINDOW_MODAL);
        janela.setTitle("Caracterização de FET — " + experimento.getNomeExperimento());

        Map<Long, CurvaFet> configuracoes = new HashMap<>();
        for (CurvaDisponivel item : curvas) {
            if (item.configuracao() != null) {
                configuracoes.put(item.curva().getId(), item.configuracao());
            }
        }
        ComboBox<CurvaDisponivel> curva = comboCurvas(curvas, configuracoes);
        ComboBox<TipoCurvaFet> tipo = comboTipos();
        TextField tensaoConstante = campo("");

        TextField ohmicoMinimo = campo("0");
        TextField ohmicoMaximo = campo("1");
        TextField saturacaoMinima = campo("2");
        TextField saturacaoMaxima = campo("5");
        TextField gmMinimo = campo("0");
        TextField gmMaximo = campo("5");
        TextField vgsReferencia = campo("");
        CheckBox gmLocal = new CheckBox("Calcular g_m local com janela de três pontos");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(9);
        formulario.add(new Label("Curva:"), 0, 0);
        formulario.add(curva, 1, 0, 3, 1);
        formulario.addRow(1, new Label("Tipo da curva:"), tipo,
                new Label("Tensão constante (V):"), tensaoConstante);
        formulario.addRow(2, new Label("Região ôhmica — V_DS mínimo (V):"), ohmicoMinimo,
                new Label("V_DS máximo (V):"), ohmicoMaximo);
        formulario.addRow(3, new Label("Saturação — V_DS mínimo (V):"), saturacaoMinima,
                new Label("V_DS máximo (V):"), saturacaoMaxima);
        formulario.addRow(4, new Label("Intervalo de g_m — V_GS mínimo (V):"), gmMinimo,
                new Label("V_GS máximo (V):"), gmMaximo);
        formulario.add(gmLocal, 1, 5, 3, 1);
        formulario.addRow(6, new Label("Ganho intrínseco — V_GS de referência (V):"),
                vgsReferencia);
        configurarColunas(formulario);

        Runnable atualizarCampos = () -> {
            boolean saida = tipo.getValue() == TipoCurvaFet.SAIDA;
            ohmicoMinimo.setDisable(!saida);
            ohmicoMaximo.setDisable(!saida);
            saturacaoMinima.setDisable(!saida);
            saturacaoMaxima.setDisable(!saida);
            gmMinimo.setDisable(saida);
            gmMaximo.setDisable(saida);
            gmLocal.setDisable(saida);
        };

        Runnable carregarConfiguracao = () -> {
            CurvaDisponivel selecionada = curva.getValue();
            CurvaFet configuracao = selecionada == null ? null
                    : configuracoes.get(selecionada.curva().getId());
            if (configuracao == null) {
                tipo.getSelectionModel().select(TipoCurvaFet.SAIDA);
                tensaoConstante.clear();
            } else {
                tipo.getSelectionModel().select(configuracao.getTipoCurvaFet());
                tensaoConstante.setText(formatar(configuracao.getValorTensaoConstante()));
            }
            atualizarCampos.run();
        };
        curva.setOnAction(evento -> carregarConfiguracao.run());
        tipo.setOnAction(evento -> atualizarCampos.run());
        curva.getSelectionModel().selectFirst();
        carregarConfiguracao.run();

        TextArea relatorio = new TextArea();
        relatorio.setEditable(false);
        relatorio.setWrapText(true);
        relatorio.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        relatorio.setPromptText("Configure a curva e calcule a caracterização.");

        Button salvar = new Button("Salvar configuração da curva");
        Button calcular = new Button("Calcular caracterização");
        Button calcularFamilia = new Button("Calcular família de saída");
        Button calcularGanho = new Button("Calcular ganho intrínseco");
        Button copiar = new Button("Copiar relatório");
        Button fechar = new Button("Fechar");
        copiar.setDisable(true);

        salvar.setOnAction(evento -> {
            try {
                CurvaDisponivel selecionada = exigirCurva(curva);
                CurvaFet configuracao = controller.salvarConfiguracao(
                        idExperimento, selecionada.curva(), tipo.getValue(),
                        numero(tensaoConstante));
                configuracoes.put(selecionada.curva().getId(), configuracao);
                curva.setConverter(conversorCurvas(configuracoes));
                informar("A configuração da curva de FET foi salva.");
            } catch (IllegalArgumentException | SecurityException erro) {
                erro(erro.getMessage());
            } catch (SQLException erro) {
                erro("Não foi possível salvar a configuração da curva de FET.");
            }
        });

        calcular.setOnAction(evento -> {
            try {
                CurvaDisponivel selecionada = exigirCurva(curva);
                CurvaFet configuracao = configuracoes.get(selecionada.curva().getId());
                if (configuracao == null
                        || configuracao.getTipoCurvaFet() != tipo.getValue()
                        || Double.compare(configuracao.getValorTensaoConstante(),
                                numero(tensaoConstante)) != 0) {
                    throw new IllegalArgumentException(
                            "Salve a configuração atual da curva antes de calcular.");
                }
                List<CaracterizacaoFetCtlr.PontoFet> pontos =
                        controller.carregarPontos(selecionada.curva());
                if (tipo.getValue() == TipoCurvaFet.SAIDA) {
                    ResultadoSaida resultado = controller.analisarSaida(
                            pontos, numero(ohmicoMinimo), numero(ohmicoMaximo),
                            numero(saturacaoMinima), numero(saturacaoMaxima));
                    relatorio.setText(relatorioSaida(experimento, selecionada,
                            configuracao, resultado));
                } else if (gmLocal.isSelected()) {
                    List<ResultadoGmLocal> resultados =
                            controller.calcularTranscondutanciaLocal(pontos);
                    relatorio.setText(relatorioGmLocal(experimento, selecionada,
                            configuracao, resultados));
                } else {
                    ResultadoTransferencia resultado =
                            controller.analisarTransferenciaPorIntervalo(
                                    pontos, numero(gmMinimo), numero(gmMaximo));
                    relatorio.setText(relatorioTransferencia(experimento, selecionada,
                            configuracao, resultado));
                }
                copiar.setDisable(false);
            } catch (IllegalArgumentException erro) {
                erro(erro.getMessage());
            } catch (SQLException erro) {
                erro("Não foi possível carregar os pontos da curva selecionada.");
            }
        });

        calcularFamilia.setOnAction(evento -> {
            try {
                List<CurvaDisponivel> curvasAtualizadas = curvas.stream()
                        .map(item -> new CurvaDisponivel(
                                item.curva(), item.colunaX(), item.colunaY(),
                                configuracoes.get(item.curva().getId()),
                                item.quantidadePontos()))
                        .toList();
                List<ResultadoCurvaSaida> resultados = controller.analisarFamiliaSaida(
                        curvasAtualizadas, numero(ohmicoMinimo), numero(ohmicoMaximo),
                        numero(saturacaoMinima), numero(saturacaoMaxima));
                relatorio.setText(relatorioFamiliaSaida(experimento, resultados));
                copiar.setDisable(false);
            } catch (IllegalArgumentException erro) {
                erro(erro.getMessage());
            } catch (SQLException erro) {
                erro("Não foi possível carregar as curvas da família de saída.");
            }
        });

        calcularGanho.setOnAction(evento -> {
            try {
                CurvaDisponivel selecionada = exigirCurva(curva);
                List<CurvaDisponivel> curvasAtualizadas = curvas.stream()
                        .map(item -> new CurvaDisponivel(
                                item.curva(), item.colunaX(), item.colunaY(),
                                configuracoes.get(item.curva().getId()),
                                item.quantidadePontos()))
                        .toList();
                CurvaDisponivel transferencia = curvasAtualizadas.stream()
                        .filter(item -> item.curva().getId()
                                .equals(selecionada.curva().getId()))
                        .findFirst().orElseThrow();
                ResultadoGanhoIntegrado resultado = controller.analisarGanhoIntrinseco(
                        transferencia, curvasAtualizadas, numero(vgsReferencia),
                        numero(gmMinimo), numero(gmMaximo),
                        numero(saturacaoMinima), numero(saturacaoMaxima),
                        gmLocal.isSelected());
                relatorio.setText(relatorioGanhoIntrinseco(experimento, resultado));
                copiar.setDisable(false);
            } catch (IllegalArgumentException erro) {
                erro(erro.getMessage());
            } catch (SQLException erro) {
                erro("Não foi possível integrar as curvas para calcular o ganho intrínseco.");
            }
        });

        copiar.setOnAction(evento -> {
            ClipboardContent conteudo = new ClipboardContent();
            conteudo.putString(relatorio.getText());
            Clipboard.getSystemClipboard().setContent(conteudo);
        });
        fechar.setOnAction(evento -> janela.close());

        Label titulo = new Label("Caracterização genérica em unidades SI");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox botoes = new HBox(8, salvar, calcular, calcularFamilia,
                calcularGanho, copiar);
        VBox topo = new VBox(10, titulo, formulario, botoes);
        topo.setPadding(new Insets(0, 0, 12, 0));

        BorderPane raiz = new BorderPane(relatorio);
        raiz.setPadding(new Insets(14));
        raiz.setTop(topo);
        HBox rodape = new HBox(fechar);
        rodape.setPadding(new Insets(10, 0, 0, 0));
        raiz.setBottom(rodape);
        janela.setMinWidth(1100);
        janela.setMinHeight(760);
        janela.setScene(new Scene(raiz, 1160, 820));
        janela.showAndWait();
    }

    private String relatorioSaida(Experimento experimento, CurvaDisponivel curva,
                                  CurvaFet configuracao, ResultadoSaida r) {
        StringBuilder texto = cabecalho(experimento, curva, configuracao);
        texto.append("REGIÃO ÔHMICA\n")
                .append("Intervalo: ").append(formatar(r.tensaoMinimaOhmica()))
                .append(" V a ").append(formatar(r.tensaoMaximaOhmica())).append(" V\n")
                .append("Pontos: ").append(r.pontosOhmicos()).append('\n')
                .append("G_DS,ômica: ").append(formatar(r.condutanciaOhmica())).append(" S\n")
                .append("R_DS,ômica: ").append(formatarResistencia(r.resistenciaOhmica())).append('\n')
                .append("R²: ").append(formatarOpcional(r.ajusteOhmico().rQuadrado())).append("\n\n")
                .append("REGIÃO DE SATURAÇÃO\n")
                .append("Intervalo: ").append(formatar(r.tensaoMinimaSaturacao()))
                .append(" V a ").append(formatar(r.tensaoMaximaSaturacao())).append(" V\n")
                .append("Pontos: ").append(r.pontosSaturacao()).append('\n')
                .append("g_ds: ").append(formatar(r.condutanciaSaida())).append(" S\n")
                .append("r_o: ").append(formatarResistencia(r.resistenciaSaida())).append('\n')
                .append("R²: ").append(formatarOpcional(r.ajusteSaturacao().rQuadrado())).append("\n\n")
                .append("TRANSIÇÃO\n");
        if (r.joelho() == null) {
            texto.append("Tensão de joelho operacional: não calculada\n");
        } else {
            texto.append("Tensão de joelho operacional: ")
                    .append(formatar(r.joelho().tensao())).append(" V\n")
                    .append("Corrente no joelho: ")
                    .append(formatar(r.joelho().corrente())).append(" A\n");
        }
        adicionarAdvertencias(texto, r.advertencias());
        return texto.toString();
    }

    private String relatorioTransferencia(Experimento experimento,
                                           CurvaDisponivel curva,
                                           CurvaFet configuracao,
                                           ResultadoTransferencia r) {
        StringBuilder texto = cabecalho(experimento, curva, configuracao);
        texto.append("TRANSCONDUTÂNCIA POR INTERVALO\n")
                .append("Intervalo: ").append(formatar(r.tensaoMinima()))
                .append(" V a ").append(formatar(r.tensaoMaxima())).append(" V\n")
                .append("Pontos: ").append(r.pontosSelecionados()).append('\n')
                .append("g_m: ").append(formatar(r.transcondutancia())).append(" S\n")
                .append("R²: ").append(formatarOpcional(r.ajuste().rQuadrado())).append('\n');
        adicionarAdvertencias(texto, r.advertencias());
        return texto.toString();
    }

    private String relatorioFamiliaSaida(Experimento experimento,
                                          List<ResultadoCurvaSaida> resultados) {
        StringBuilder texto = new StringBuilder(
                "RELATÓRIO CONSOLIDADO — FAMÍLIA DE CURVAS DE SAÍDA DE FET\n\n")
                .append("Experimento: ").append(experimento.getNomeExperimento()).append('\n')
                .append("Curvas analisadas: ").append(resultados.size()).append("\n\n");

        for (ResultadoCurvaSaida item : resultados) {
            CurvaDisponivel curva = item.curva();
            CurvaFet configuracao = curva.configuracao();
            ResultadoSaida r = item.resultado();
            texto.append("CURVA: ").append(curva.curva().getNome()).append('\n')
                    .append("V_GS constante: ")
                    .append(formatar(configuracao.getValorTensaoConstante())).append(" V\n")
                    .append("Pontos válidos: ").append(r.pontosValidos()).append('\n')
                    .append("Região ôhmica: ").append(formatar(r.tensaoMinimaOhmica()))
                    .append(" V a ").append(formatar(r.tensaoMaximaOhmica())).append(" V; ")
                    .append(r.pontosOhmicos()).append(" pontos\n")
                    .append("G_DS,ômica: ").append(formatar(r.condutanciaOhmica()))
                    .append(" S; R_DS,ômica: ")
                    .append(formatarResistencia(r.resistenciaOhmica())).append("; R²: ")
                    .append(formatarOpcional(r.ajusteOhmico().rQuadrado())).append('\n')
                    .append("Saturação: ").append(formatar(r.tensaoMinimaSaturacao()))
                    .append(" V a ").append(formatar(r.tensaoMaximaSaturacao())).append(" V; ")
                    .append(r.pontosSaturacao()).append(" pontos\n")
                    .append("g_ds: ").append(formatar(r.condutanciaSaida()))
                    .append(" S; r_o: ").append(formatarResistencia(r.resistenciaSaida()))
                    .append("; R²: ")
                    .append(formatarOpcional(r.ajusteSaturacao().rQuadrado())).append('\n');
            if (r.joelho() == null) {
                texto.append("Joelho operacional: não calculado\n");
            } else {
                texto.append("Joelho operacional: V_DS = ")
                        .append(formatar(r.joelho().tensao())).append(" V; I_D = ")
                        .append(formatar(r.joelho().corrente())).append(" A\n");
            }
            adicionarAdvertencias(texto, r.advertencias());
            texto.append("\n----------------------------------------\n\n");
        }
        return texto.toString();
    }

    private String relatorioGanhoIntrinseco(Experimento experimento,
                                             ResultadoGanhoIntegrado r) {
        double gm = r.resultadoGmLocal() == null
                ? r.resultadoTransferencia().transcondutancia()
                : r.resultadoGmLocal().transcondutancia();
        return new StringBuilder("RELATÓRIO DE GANHO INTRÍNSECO DE FET\n\n")
                .append("Experimento: ").append(experimento.getNomeExperimento()).append('\n')
                .append("Curva de transferência: ")
                .append(r.curvaTransferencia().curva().getNome()).append('\n')
                .append("V_DS constante: ")
                .append(formatar(r.curvaTransferencia().configuracao()
                        .getValorTensaoConstante())).append(" V\n")
                .append("Curva de saída: ").append(r.curvaSaida().curva().getNome()).append('\n')
                .append("V_GS de referência: ").append(formatar(r.vgsReferencia()))
                .append(" V\n\n")
                .append("g_m: ").append(formatar(gm)).append(" S\n")
                .append("Método de g_m: ").append(r.ganho().classificacao()).append('\n')
                .append("g_ds: ").append(formatar(
                        r.resultadoSaida().condutanciaSaida())).append(" S\n")
                .append("r_o: ").append(formatarResistencia(
                        r.resultadoSaida().resistenciaSaida())).append('\n')
                .append("A_v0 = g_m r_o: ").append(formatarGanho(r.ganho().ganho()))
                .append(" V/V\n")
                .append("A_v0: ").append(formatarGanho(r.ganho().ganhoDecibeis()))
                .append(" dB\n")
                .toString();
    }

    private String relatorioGmLocal(Experimento experimento, CurvaDisponivel curva,
                                    CurvaFet configuracao,
                                    List<ResultadoGmLocal> resultados) {
        StringBuilder texto = cabecalho(experimento, curva, configuracao);
        texto.append("TRANSCONDUTÂNCIA LOCAL — JANELA DE TRÊS PONTOS\n")
                .append("V_GS (V)              g_m (S)               R²\n");
        for (ResultadoGmLocal resultado : resultados) {
            texto.append(formatar(resultado.vgs())).append("    ")
                    .append(formatar(resultado.transcondutancia())).append("    ")
                    .append(formatarOpcional(resultado.ajuste().rQuadrado())).append('\n');
            adicionarAdvertencias(texto, resultado.advertencias());
        }
        return texto.toString();
    }

    private StringBuilder cabecalho(Experimento experimento, CurvaDisponivel curva,
                                     CurvaFet configuracao) {
        String condicao = configuracao.getTipoCurvaFet() == TipoCurvaFet.SAIDA
                ? "V_GS" : "V_DS";
        return new StringBuilder("RELATÓRIO DE CARACTERIZAÇÃO DE FET\n\n")
                .append("Experimento: ").append(experimento.getNomeExperimento()).append('\n')
                .append("Curva: ").append(curva.curva().getNome()).append('\n')
                .append("Tipo: ").append(nomeTipo(configuracao.getTipoCurvaFet())).append('\n')
                .append(condicao).append(" constante: ")
                .append(formatar(configuracao.getValorTensaoConstante())).append(" V\n")
                .append("Pontos válidos: ").append(curva.quantidadePontos()).append("\n\n");
    }

    private void adicionarAdvertencias(StringBuilder texto, List<String> advertencias) {
        if (advertencias.isEmpty()) {
            return;
        }
        texto.append("\nADVERTÊNCIAS\n");
        for (String advertencia : advertencias) {
            texto.append("- ").append(advertencia).append('\n');
        }
    }

    private ComboBox<CurvaDisponivel> comboCurvas(List<CurvaDisponivel> curvas,
                                                   Map<Long, CurvaFet> configuracoes) {
        ComboBox<CurvaDisponivel> combo = new ComboBox<>(
                FXCollections.observableArrayList(curvas));
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(conversorCurvas(configuracoes));
        return combo;
    }

    private StringConverter<CurvaDisponivel> conversorCurvas(
            Map<Long, CurvaFet> configuracoes) {
        return new StringConverter<>() {
            @Override
            public String toString(CurvaDisponivel item) {
                if (item == null) {
                    return "";
                }
                CurvaFet configuracao = configuracoes.get(item.curva().getId());
                String estado = configuracao == null
                        ? "não configurada"
                        : nomeTipo(configuracao.getTipoCurvaFet());
                return item.curva().getNome() + " — "
                        + item.colunaX().getNomeColuna() + " × "
                        + item.colunaY().getNomeColuna() + " — "
                        + item.quantidadePontos() + " pontos — " + estado;
            }

            @Override
            public CurvaDisponivel fromString(String texto) {
                return null;
            }
        };
    }

    private ComboBox<TipoCurvaFet> comboTipos() {
        ComboBox<TipoCurvaFet> combo = new ComboBox<>(
                FXCollections.observableArrayList(TipoCurvaFet.values()));
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoCurvaFet valor) {
                return valor == null ? "" : nomeTipo(valor);
            }

            @Override
            public TipoCurvaFet fromString(String texto) {
                return null;
            }
        });
        return combo;
    }

    private String nomeTipo(TipoCurvaFet tipo) {
        return tipo == TipoCurvaFet.SAIDA ? "Saída — I_D × V_DS"
                : "Transferência — I_D × V_GS";
    }

    private TextField campo(String valor) {
        TextField campo = new TextField(valor);
        campo.setPrefColumnCount(12);
        return campo;
    }

    private void configurarColunas(GridPane formulario) {
        for (int indice = 0; indice < 4; indice++) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setMinWidth(indice % 2 == 0 ? 245 : 175);
            if (indice % 2 == 1) {
                coluna.setHgrow(Priority.ALWAYS);
            }
            formulario.getColumnConstraints().add(coluna);
        }
        GridPane.setHgrow(formulario.getChildren().get(1), Priority.ALWAYS);
    }

    private CurvaDisponivel exigirCurva(ComboBox<CurvaDisponivel> combo) {
        CurvaDisponivel selecionada = combo.getValue();
        if (selecionada == null) {
            throw new IllegalArgumentException("Selecione uma curva.");
        }
        return selecionada;
    }

    private double numero(TextField campo) {
        if (campo.getText() == null || campo.getText().isBlank()) {
            throw new IllegalArgumentException("Preencha os parâmetros numéricos necessários.");
        }
        return FormatadorNumero.converter(campo.getText());
    }

    private String formatar(double valor) {
        return FormatadorNumero.formatar(valor);
    }

    private String formatarOpcional(Double valor) {
        return valor == null ? "não definido" : formatar(valor);
    }

    private String formatarResistencia(Double valor) {
        if (valor == null) {
            return "não definida";
        }
        if (Double.isInfinite(valor)) {
            return "teoricamente infinita";
        }
        return formatar(valor) + " ohm";
    }

    private String formatarGanho(double valor) {
        if (Double.isInfinite(valor)) {
            return valor > 0.0 ? "teoricamente infinito"
                    : "teoricamente menos infinito";
        }
        return formatar(valor);
    }

    private void informar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(janelaPai);
        alerta.setTitle("GACS");
        alerta.setHeaderText("Caracterização de FET");
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    private void erro(String texto) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(janelaPai);
        alerta.setTitle("GACS");
        alerta.setHeaderText("Caracterização de FET não concluída");
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}
