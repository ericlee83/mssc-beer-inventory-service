package guru.sfg.beer.inventory.service.services;

import com.springframework.brewery.model.events.AllocateOrderRequest;
import com.springframework.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import static guru.sfg.beer.inventory.service.config.JmsConfig.ALLOCATE_ORDER;
import static guru.sfg.beer.inventory.service.config.JmsConfig.ALLOCATE_ORDER_RESPONSE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocationListener {

    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = ALLOCATE_ORDER)
    public void listen(AllocateOrderRequest request){
        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();

        builder.beerOrder(request.getBeerOrder());

        try{
            Boolean allocationResult = allocationService.allocateOrder(request.getBeerOrder());
            if(allocationResult){
                builder.pendingInventory(false);
            }else{
                builder.pendingInventory(true);
            }
            builder.allocationError(false);
        }catch(Exception e){
            log.error("Allocation failed for Order id: "+request.getBeerOrder().getId());
            builder.allocationError(true);
        }

        jmsTemplate.convertAndSend(ALLOCATE_ORDER_RESPONSE,builder.build());
    }
}
