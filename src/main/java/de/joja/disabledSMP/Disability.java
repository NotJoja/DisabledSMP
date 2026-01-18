package de.joja.disabledSMP;

public enum Disability {

    KURZSICHTIGKEIT,
    NUR_EIN_ARM,
    KLEINWUECHSIGKEIT,
    HERZINFARKTE,
    GEHBEHINDERT,
    KURZZEITGEDAECHTNIS,
    FARBENBLINDHEIT,
    GLEICHGEWICHTSPROBLEME,
    TAUB,
    ZITTERANFAELLE,
    UEBERGEWICHT,
    KEINE_ZAEHNE,
    ALLERGIE,
    SCHIZOPHRENIE,
    RASSISMUS;

    public static final int DISABILITIES_TOTAL_AMOUNT = Disability.values().length;

    public static Disability get(int i) {
        return values()[i];
    }
}
