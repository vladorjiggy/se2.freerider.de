package de.freerider.restapi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import de.freerider.datamodel.Customer;
import de.freerider.repository.CustomerRepository;

@RestController
class CustomersController implements CustomersAPI {

    @Autowired // Spring auto‐wires reference to CustomerRepository instance
    private CustomerRepository customerRepository;
    private ApplicationContext context;
    //
    private final ObjectMapper objectMapper;
    //
    private final HttpServletRequest request;

    private final Iterable<Customer> customers;

    /**
     * Constructor.
     * 
     * @param objectMapper entry point to JSON tree for the Jackson library
     * @param request      HTTP request object
     */
    @Autowired
    public CustomersController(ObjectMapper objectMapper, HttpServletRequest request, ApplicationContext context,
            CustomerRepository customerRepository) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.context = context;
        this.customerRepository = customerRepository;
        this.customers = customerRepository.findAll();
    }

    @Override
    public ResponseEntity<List<?>> getCustomers() {
        ResponseEntity<List<?>> re = null;
        System.err.println(request.getMethod() + " " + request.getRequestURI());
        try {
            ArrayNode arrayNode = customersAsJSON();
            ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ObjectNode>>() {
            });
            List<String> list = reader.readValue(arrayNode);
            //
            re = new ResponseEntity<List<?>>(list, HttpStatus.OK);

        } catch (IOException e) {
            re = new ResponseEntity<List<?>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return re;
    }

    @Override
    public ResponseEntity<?> getCustomer(long id) {
        ResponseEntity<Object> response = null;
        System.err.println(request.getMethod() + " " + request.getRequestURI());
        try {
            ObjectNode objectNode = customerAsJSON(id);
            if (objectNode.size() == 0) {
                response = new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
            } else {
                ObjectReader reader = objectMapper.readerFor(new TypeReference<ObjectNode>() {
                });
                Object customer = reader.readValue(objectNode);
                response = new ResponseEntity<Object>(customer, HttpStatus.OK);
            }
        } catch (IOException e) {
            response = new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private ArrayNode customersAsJSON() {
        System.out.println(customers);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Customer customer : customers) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("id", customer.getId());
            objectNode.put("firstName", customer.getFirstName());
            objectNode.put("lastName", customer.getLastName());
            Iterable<String> contacts = customer.getContacts();
            StringBuffer sb = new StringBuffer();
            contacts.forEach(contact -> sb.append(sb.length() == 0 ? "" : "; ").append(contact));
            objectNode.put("contacts", sb.toString());
            arrayNode.add(objectNode);
        }
        return arrayNode;
    }

    private ObjectNode customerAsJSON(long id) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                objectNode.put("id", customer.getId());
                objectNode.put("firstName", customer.getFirstName());
                objectNode.put("lastName", customer.getLastName());
                Iterable<String> contacts = customer.getContacts();
                StringBuffer sb = new StringBuffer();
                contacts.forEach(contact -> sb.append(sb.length() == 0 ? "" : "; ").append(contact));
                objectNode.put("contacts", sb.toString());
            }
        }
        return objectNode;
    }

    @Override
    public ResponseEntity<?> deleteCustomer(long id) {
        ResponseEntity<Object> response = null;
        System.err.println(request.getMethod() + " " + request.getRequestURI());
        if (customerRepository.existsById(id)) {
            try {
                customerRepository.deleteById(id);
                response = new ResponseEntity<Object>(HttpStatus.ACCEPTED);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                response = new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response = new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseEntity<List<?>> postCustomers(Map<String, Object>[] jsonMap) {
        if( jsonMap == null ){
            return new ResponseEntity<List<?>>(HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<List<?>> response = null;
        System.err.println(request.getMethod() + " " + request.getRequestURI());
        try {
            for (Map<String, Object> map : jsonMap) {
                Optional<Customer> customer = accept(map);
                System.out.println(customer);
                if(!customer.isPresent()){
                    System.err.println("Customer is null");
                    return new ResponseEntity<List<?>>(HttpStatus.BAD_REQUEST);
                }
                else{
                    if(customerRepository.existsById(customer.get().getId())){
                        System.err.println("Customer already exists");
                        return new ResponseEntity<List<?>>(HttpStatus.CONFLICT);
                    }
                    else{
                        customerRepository.save(customer.get());
                    }
                }
                
                
            }
            response = new ResponseEntity<List<?>>(HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response = new ResponseEntity<List<?>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseEntity<List<?>> putCustomers(long id, Map<String, Object>[] jsonMap) {
        ResponseEntity<List<?>> response = null;
        System.err.println(request.getMethod() + " " + request.getRequestURI());
        try {
            for (Map<String, Object> map : jsonMap) {
                Customer customer = customerRepository.findById(id).get();
                customer.setName((String) map.get("first"), (String) map.get("name"));
                customer.addContact((String) map.get("contacts"));
                customerRepository.save(customer);
            }
            response = new ResponseEntity<List<?>>(HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response = new ResponseEntity<List<?>>(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    private Optional<Customer> accept( Map<String, Object> kvpairs ) {  
        if(kvpairs.containsKey("id")){
            Long id = ((Number) kvpairs.get("id")).longValue();
            if(id <= 0 || !hasNames(kvpairs)){
                return Optional.empty();
            }
            Customer customer = createCustomer(id, (String) kvpairs.get("first"), (String) kvpairs.get("name"), (String) kvpairs.get("contacts"));
            return Optional.of(customer);

        }
        else{
            if(!hasNames(kvpairs)){
                return Optional.empty();
            }
            Iterable<Customer> customers = customerRepository.findAll();
            Long maxId = 0L;
            for( Customer customer : customers ){
                if( customer.getId() > maxId ){
                    maxId = customer.getId();
                }
            }
            Long id = maxId + 1;
            Customer customer = createCustomer(id, (String) kvpairs.get("first"), (String) kvpairs.get("name"), (String) kvpairs.get("contacts"));
            return Optional.of(customer);
        }
        
    }

    private Customer createCustomer(Long id, String first, String last, String contacts) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(first, last);
        customer.addContact(contacts);
        return customer;
    }

    private boolean hasNames(Map<String, Object> kvpairs) {
        return kvpairs.containsKey("first") && kvpairs.containsKey("name");
    }

}
