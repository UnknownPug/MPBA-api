package api.mpba.rastvdmy.service.impl;

/**
 * Enum representing supported banks.
 */
public enum SupportedBanks {
    CESKA_SPORITELNA("Ceska Sporitelna"),
    CSOB("CSOB"),
    FIO_BANKA("Fio Banka"),
    KOMERCNI_BANKA("Komercni Banka"),
    MONETA_MONEY_BANK("Moneta Money Bank"),
    RAIFFEISENBANK("Raiffeisenbank"),
    MONOBANK("Monobank"),
    PRIVATBANK("Privatbank"),
    SLOVENSKA_SPORITELNA("Slovenska Sporitelna"),
    UNICREDIT_BANK("Unicredit Bank");

    private final String bankName;

    /**
     * Constructor for SupportedBanks enum.
     *
     * @param bankName the name of the bank
     */
    SupportedBanks(String bankName) {
        this.bankName = bankName;
    }

    /**
     * Returns the name of the bank.
     *
     * @return the name of the bank
     */
    @Override
    public String toString() {
        return bankName;
    }

    /**
     * Returns a random bank name from the supported banks.
     *
     * @return a random bank name
     */
    public static String getRandomBank() {
        return SupportedBanks.values()[(int) (Math.random() * SupportedBanks.values().length)].toString();
    }
}
