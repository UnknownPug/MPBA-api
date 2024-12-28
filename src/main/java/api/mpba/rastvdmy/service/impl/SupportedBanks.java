package api.mpba.rastvdmy.service.impl;

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

    SupportedBanks(String bankName) {
        this.bankName = bankName;
    }

    @Override
    public String toString() {
        return bankName;
    }

    public static String getRandomBank() {
        return SupportedBanks.values()[(int) (Math.random() * SupportedBanks.values().length)].toString();
    }
}
