package api.mpba.rastvdmy.service.impl.factory;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.Payment;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import api.mpba.rastvdmy.service.impl.PurchaseCategory;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

import static api.mpba.rastvdmy.entity.enums.Currency.getRandomCurrency;

/**
 * Factory class for creating card payment objects.
 */
public class CardPayment implements PaymentProcess {
    private static final int MAX_PAYMENT = 6000; // Maximum amount for a card payment

    /**
     * Creates a Payment object for a card payment.
     *
     * @param senderAccount    the bank account of the sender
     * @param description      a description for the payment
     * @param recipientAccount the bank account of the recipient
     * @param card             the card used for the payment
     * @return the created Payment object
     * @throws Exception if an error occurs during payment creation
     */
    @Override
    public Payment createPayment(BankAccount senderAccount, String description,
                                 BankAccount recipientAccount, Card card) throws Exception {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encryptedSenderName = EncryptionUtil.encrypt(
                senderAccount.getBankIdentity().getUserProfile().getName() + " "
                        + senderAccount.getBankIdentity().getUserProfile().getSurname(), secretKey);

        String encryptedRecipientName = EncryptionUtil.encrypt(PurchaseCategory.getRandomCategory(), secretKey);

        return Payment.builder()
                .id(UUID.randomUUID())
                .senderName(encryptedSenderName)
                .recipientName(encryptedRecipientName)
                .dateTime(LocalDate.now())
                .amount(generateRandomAmount())
                .type(PaymentType.CARD_PAYMENT)
                .currency(getRandomCurrency())
                .senderCard(card)
                .build();
    }

    /**
     * Generates a random amount for the payment.
     *
     * @return a random BigDecimal amount between 1 and MAX_PAYMENT
     */
    private BigDecimal generateRandomAmount() {
        Random randomAmount = new Random();
        return BigDecimal.valueOf(randomAmount.nextDouble(MAX_PAYMENT) + 1);
    }
}