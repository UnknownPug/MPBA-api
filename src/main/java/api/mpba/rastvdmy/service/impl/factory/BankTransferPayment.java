package api.mpba.rastvdmy.service.impl.factory;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.Payment;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import org.apache.commons.text.StringEscapeUtils;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Factory class for creating bank transfer payment objects.
 */
public class BankTransferPayment implements PaymentProcess {

    /**
     * Creates a Payment object for a bank transfer.
     *
     * @param senderAccount    the bank account of the sender
     * @param description      a description for the payment
     * @param recipientAccount the bank account of the recipient
     * @param card             the card used for the payment (not used in bank transfer)
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

        String encryptedRecipientName = EncryptionUtil.encrypt(
                recipientAccount.getBankIdentity().getUserProfile().getName() + " "
                        + recipientAccount.getBankIdentity().getUserProfile().getSurname(), secretKey);

        String sanitizedDescription = StringEscapeUtils.escapeHtml4(description.trim());
        String encryptedDescription = EncryptionUtil.encrypt(sanitizedDescription, secretKey);

        return Payment.builder()
                .id(UUID.randomUUID())
                .senderName(encryptedSenderName)
                .recipientName(encryptedRecipientName)
                .dateTime(LocalDate.now())
                .description(encryptedDescription)
                .type(PaymentType.BANK_TRANSFER)
                .senderAccount(senderAccount)
                .recipientAccount(recipientAccount)
                .build();
    }
}