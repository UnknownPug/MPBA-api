package api.mpba.rastvdmy.service.impl.factory;

import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.Payment;

/**
 * Factory interface for creating Payment objects.
 */
public interface PaymentProcess {
    /**
     * Creates a Payment object.
     *
     * @param senderAccount    the bank account of the sender
     * @param description      a description for the payment
     * @param recipientAccount the bank account of the recipient
     * @param card             the card used for the payment
     * @return the created Payment object
     * @throws Exception if an error occurs during payment creation
     */
    Payment createPayment(BankAccount senderAccount, String description,
                          BankAccount recipientAccount, Card card) throws Exception;
}