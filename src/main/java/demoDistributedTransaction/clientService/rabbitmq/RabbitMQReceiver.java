package demoDistributedTransaction.clientService.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

//******not used********
@Component
public class RabbitMQReceiver {
	@RabbitListener(queues = "${queueNameBank}")
	public void receivedMessage(String message) {
		System.out.println("Received msg:\n"+message);
		
	}
		

}
