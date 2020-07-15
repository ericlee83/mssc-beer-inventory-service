package guru.sfg.beer.inventory.service.services;

import com.springframework.brewery.model.BeerOrderDto;

public interface AllocationService {

    public Boolean allocateOrder(BeerOrderDto beerOrderDto);

    void deallocateOrder(BeerOrderDto beerOrder);
}
