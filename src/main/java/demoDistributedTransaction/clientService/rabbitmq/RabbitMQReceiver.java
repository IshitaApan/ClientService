package demoDistributedTransaction.clientService.rabbitmq;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import demoDistributedTransaction.clientService.CustomerLedger;
import demoDistributedTransaction.clientService.CustomerMaster;
import demoDistributedTransaction.clientService.repository.CustomerLedgerRepository;
import demoDistributedTransaction.clientService.repository.CustomerMasterRepository;

@Component
public class RabbitMQReceiver {
	@Autowired
	private CustomerLedgerRepository customerLedgerRepository;
	@Autowired
	private CustomerMasterRepository customerMasterRepository;
	
	@RabbitListener(queues = "${queueNameBank}")
	public void receivedMessage(CustomerLedger customerLedger) {
		updateCustomerLedgerAndMaster(customerLedger);
		
		System.out.println("CustomerLedger status:\n"+customerLedger.getStatus());
		
	}
	public void updateCustomerLedgerAndMaster(CustomerLedger customerLedger){
		//update customer ledger 
		customerLedgerRepository.save(customerLedger);
		
		//update customerMaster
		CustomerMaster customerMaster = null; 
		List<CustomerMaster> listOfCustomerMasters = customerMasterRepository.findAll();
		for(int i=0;i<listOfCustomerMasters.size();i++) {
			if(listOfCustomerMasters.get(i).getCustomerId().equals(customerLedger.getCustomerId())) {
				customerMaster = listOfCustomerMasters.get(i);
				break;
			}
		}
		Double customerMasterBalance = customerMaster.getBalance();
		
		if(customerLedger.getTransactionType().equals("Withdraw")) {
			if(customerMasterBalance < customerLedger.getAmount()) {
				System.out.println("Failure!!!! Withdrawal not possible due to lack of balance");
				return;
			}
			customerMaster.setBalance(customerMasterBalance-customerLedger.getAmount());
			customerMaster.setLastTransactionDate(customerLedger.getTransactionDate());
			
		}
		else if(customerLedger.getTransactionType().equals("Deposit")) {
			customerMaster.setBalance(customerMasterBalance+customerLedger.getAmount());
			customerMaster.setLastTransactionDate(customerLedger.getTransactionDate());
		}
		
		customerMasterRepository.save(customerMaster);
		
				
	}
		

}
