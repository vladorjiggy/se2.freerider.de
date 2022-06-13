package de.freerider.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import de.freerider.datamodel.Customer;

@Component
public class CustomerRepository implements CrudRepository<Customer, Long> {

    private List<Customer> customers = new ArrayList<Customer>();

    @Override
    public <S extends Customer> S save(S entity) {
        if(entity != null){
            if(!this.customers.contains(entity)){
                this.customers.add(entity);
            }            
            return entity;
        }
        else{
            throw new IllegalArgumentException("Customer must not be null");
        }        
    }

    @Override
    public <S extends Customer> Iterable<S> saveAll(Iterable<S> entities) {
        if(entities != null){
            for(S entity : entities){
                if(entity != null){
                    this.customers.add(entity);
                }
                else{
                    throw new IllegalArgumentException("Customer must not be null");
                }
            }
            return entities;
        }
        else{
            throw new IllegalArgumentException("Customer must not be null");
        }
    }

    @Override
    public boolean existsById(Long id) {
        if(id != null){
            for(Customer customer : this.customers){
                if(customer.getId() == id){
                    return true;
                }
            }
            return false;
        }
        else{
            throw new IllegalArgumentException("ID must not be null");
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        if(id != null){
            for(Customer customer : this.customers){
                if(customer.getId() == id){
                    return Optional.of(customer); // weiß nicht ob das richtig ist
                }
            }
            return Optional.empty();
        }
        else{
            throw new IllegalArgumentException("ID must not be null");
        }
    }

    @Override
    public Iterable<Customer> findAll() {
        return this.customers;
    }

    @Override
    public Iterable<Customer> findAllById(Iterable<Long> ids) {
        if(ids != null){
            List<Customer> foundCustomers = new ArrayList<Customer>();
            for(Long id : ids){
                if(id != null){
                    for(Customer customer : this.customers){
                        if(customer.getId() == id){
                            foundCustomers.add(customer);
                        }
                    }
                }
                else{
                    throw new IllegalArgumentException("ID must not be null");
                }
            }
            return foundCustomers;
        }
        else{
            throw new IllegalArgumentException("IDs must not be null");
        }
    }

    @Override
    public long count() {
        return this.customers.size();
    }

    @Override
    public void deleteById(Long id) {
        if(id != null){
            System.out.println("id: " + id);

            Optional <Customer> customer = findById(id);
            if(customer.isPresent()){
                this.customers.remove(customer.get());
            }
        }
        else{
            throw new IllegalArgumentException("ID must not be null");
        }        
    }

    @Override
    public void delete(Customer entity) { // das ist wahrscheinlich nicht richtig -> ich denke ich muss einen vergleich machen
        if(entity != null){
            this.customers.remove(entity);
        }
        else{
            throw new IllegalArgumentException("Customer must not be null");
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        if(ids != null){
            for(Long id : ids){
                if(id != null){
                    for(Customer customer : this.customers){
                        if(customer.getId() == id){
                            this.customers.remove(customer);
                        }
                    }
                }
                else{
                    throw new IllegalArgumentException("ID must not be null");
                }
            }
        }
        else{
            throw new IllegalArgumentException("IDs must not be null");
        }        
    }

    @Override
    public void deleteAll(Iterable<? extends Customer> entities) { // das ist wahrscheinlich nicht richtig -> ich denke ich muss einen vergleich machen
        if(entities != null){
            for(Customer customer : entities){
                if(customer != null){
                    this.customers.remove(customer);
                }
                else{
                    throw new IllegalArgumentException("Customer must not be null");
                }
            }
        }
        else{
            throw new IllegalArgumentException("Customers must not be null");
        }        
    }

    @Override
    public void deleteAll() {
        this.customers.clear();        
    }
    
} 
