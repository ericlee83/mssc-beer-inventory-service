package guru.sfg.beer.inventory.service.services;

import com.springframework.brewery.model.BeerOrderDto;
import com.springframework.brewery.model.BeerOrderLineDto;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;
    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating orderId: "+beerOrderDto.getId());
        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();
        beerOrderDto.getBeerOrderLines().forEach(orderLine->{
            if((orderLine.getOrderQuantity() != null ? orderLine.getOrderQuantity(): 0 )-
                    (orderLine.getQuantityAllocated() != null ? orderLine.getQuantityAllocated() : 0) >0){
                allocateBeerOrderLine(orderLine);
            }
            totalOrdered.set(totalOrdered.get()+orderLine.getOrderQuantity());
            totalAllocated.set(totalAllocated.get()+orderLine.getQuantityAllocated());
        });
        log.debug("total ordered: "+totalOrdered.get()+" total allocated: "+totalAllocated.get());
        return totalOrdered.get() == totalAllocated.get();
    }

    @Override
    public void deallocateOrder(BeerOrderDto beerOrder) {
        log.debug("Deallocating orderId: "+beerOrder.getId());
    }

    private void allocateBeerOrderLine(BeerOrderLineDto orderLine) {
        List<BeerInventory> beerInventories = beerInventoryRepository.findAllByUpc(orderLine.getUpc());
        beerInventories.forEach(beerInventory -> {
            int inventory = (beerInventory.getQuantityOnHand() == null ) ? 0 : beerInventory.getQuantityOnHand();
            int orderQty = (orderLine.getOrderQuantity() == null) ? 0 : orderLine.getOrderQuantity();
            int allocatedQty = (orderLine.getQuantityAllocated() == null )? 0: orderLine.getQuantityAllocated();
            int qtyToAllocate = orderQty - allocatedQty;
            if(inventory >= qtyToAllocate){
                inventory = inventory - qtyToAllocate;
                orderLine.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);
                beerInventoryRepository.save(beerInventory);
            }else if(inventory > 0){
                orderLine.setQuantityAllocated(allocatedQty+ inventory);
                beerInventory.setQuantityOnHand(0);
            }
            if(beerInventory.getQuantityOnHand() == 0){
                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
