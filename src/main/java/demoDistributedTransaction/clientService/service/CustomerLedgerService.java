package demoDistributedTransaction.clientService.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import demoDistributedTransaction.clientService.CustomerLedger;
import demoDistributedTransaction.clientService.CustomerMaster;
import demoDistributedTransaction.clientService.rabbitmq.RabbitMQSender;
import demoDistributedTransaction.clientService.repository.CustomerLedgerRepository;
import demoDistributedTransaction.clientService.repository.CustomerMasterRepository;

@Service
public class CustomerLedgerService {
	@Autowired
	private CustomerLedgerRepository customerLedgerRepository;
	
	@Autowired
	private CustomerMasterRepository customerMasterRepository;
	
	@Autowired
	private RabbitMQSender rabbitMQSender;
	
	public List<CustomerLedger> getAllFromCustomerLedger(){
		List<CustomerLedger> customerLedgerList = new ArrayList<CustomerLedger>();
		customerLedgerRepository.findAll().forEach(customerLedgerList::add);
		return customerLedgerList;
	}
	
	public Optional<CustomerLedger> getFromCustomerLedger(Long id) {
		return customerLedgerRepository.findById(id);
	}
	
	public void addToCustomerLedger(CustomerLedger customerLedger) {
		Long customerId = customerLedger.getCustomerId();
		String transactionType = customerLedger.getTransactionType();
		Double customerLedgerAmount = customerLedger.getAmount();
		
		
		CustomerMaster customerMaster = null; 
		List<CustomerMaster> listOfCustomerMasters = customerMasterRepository.findAll();
		for(int i=0;i<listOfCustomerMasters.size();i++) {
			if(listOfCustomerMasters.get(i).getCustomerId().equals(customerId)) {
				customerMaster = listOfCustomerMasters.get(i);
				break;
			}
		}
		Double customerMasterBalance = customerMaster.getBalance();
		
		if(transactionType.equals("Withdraw")) {
			if(customerMasterBalance < customerLedgerAmount) {
				System.out.println("Withdrawal not possible due to lack of balance");
				return;
			}
			customerMaster.setBalance(customerMasterBalance-customerLedgerAmount);
			
		}
		else if(transactionType.equals("Deposit")) {
			customerMaster.setBalance(customerMasterBalance+customerLedgerAmount);
		}
		
		customerLedgerRepository.save(customerLedger);
		
		rabbitMQSender.send(customerLedger);
		
		customerMasterRepository.save(customerMaster);
		
	}
	
	public void updateCustomerLedger(Long id, CustomerLedger customerLedger) {
		customerLedgerRepository.save(customerLedger);
	}
	
	public void deleteFromCustomerLedger(Long id) {
		customerLedgerRepository.deleteById(id);
	}
}
