package br.uel.gacs.model;

/** Identifica a grandeza variável e a tensão constante de uma curva de FET. */
public enum TipoCurvaFet {
    /** Curva I_D em função de V_DS, mantendo V_GS constante. */
    SAIDA,

    /** Curva I_D em função de V_GS, mantendo V_DS constante. */
    TRANSFERENCIA
}
