package guru.sfg.beer.inventory.service.services;

import com.springframework.brewery.model.events.DeallocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static guru.sfg.beer.inventory.service.config.JmsConfig.DEALLOCATE_ORDER;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeallocationListener {

    private final AllocationService allocationService;

    @JmsListener(destination = DEALLOCATE_ORDER)
    public void listen(DeallocateOrderRequest request){
        allocationService.deallocateOrder(request.getBeerOrder());
    }
}
