package com.generic.rest.main.service;

import com.generic.rest.main.dto.AddressDTO;
import com.generic.rest.main.model.Address;
import com.generic.rest.main.model.User;
import com.generic.rest.main.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AddressService {

    private static final int MAX_ADDRESSES_PER_USER = 5;

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<AddressDTO> listAddresses(User user) {
        return addressRepository.findByUserOrderByIdAsc(user)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO createAddress(User user, AddressDTO req) {
        long count = addressRepository.countByUser(user);
        if (count >= MAX_ADDRESSES_PER_USER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has reached the maximum number of addresses (" + MAX_ADDRESSES_PER_USER + ")");
        }

        Address address = new Address();
        address.setUser(user);
        applyCreate(req, address);

        if (req.isDefault()) {
            unsetExistingDefault(user);
            address.setDefault(true);
        }

        Address saved = addressRepository.save(address);
        return toResponse(saved);
    }

    @Transactional
    public AddressDTO editAddress(User user, Long id, AddressDTO req) {
        Address address = addressRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        applyEdit(req, address);

        if (Boolean.TRUE.equals(req.isDefault())) {
            unsetExistingDefault(user);
            address.setDefault(true);
        } else if (Boolean.FALSE.equals(req.isDefault())) {
            address.setDefault(false);
        }

        Address saved = addressRepository.save(address);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAddress(User user, Long id) {
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Address not found or has been deleted"));
        addressRepository.delete(address);
    }

    private void unsetExistingDefault(User user) {
        addressRepository.findByUserAndIsDefaultTrue(user)
            .ifPresent(existing -> {
                existing.setDefault(false);
                addressRepository.save(existing);
            });
    }

    private void applyCreate(AddressDTO req, Address address) {
        address.setFirstName(req.getFirstName());
        address.setLastName(req.getLastName());
        address.setAddress(req.getAddress());
        address.setAddress2(req.getAddress2());
        address.setArea(req.getArea());
        address.setState(req.getState());
        address.setCountry(req.getCountry());
        address.setZipCode(req.getZipCode());
        address.setPhoneNumber(req.getPhoneNumber());
        address.setDefault(req.isDefault());
    }

    private void applyEdit(AddressDTO req, Address address) {
        if (StringUtils.hasText(req.getFirstName())) address.setFirstName(req.getFirstName());
        if (StringUtils.hasText(req.getLastName())) address.setLastName(req.getLastName());
        if (StringUtils.hasText(req.getAddress())) address.setAddress(req.getAddress());
        if (req.getAddress2() != null) address.setAddress2(req.getAddress2());
        if (req.getArea() != null) address.setArea(req.getArea());
        if (req.getState() != null) address.setState(req.getState());
        if (req.getCountry() != null) address.setCountry(req.getCountry());
        if (req.getZipCode() != null) address.setZipCode(req.getZipCode());
        if (req.getPhoneNumber() != null) address.setPhoneNumber(req.getPhoneNumber());
    }

    private AddressDTO toResponse(Address a) {
        AddressDTO r = new AddressDTO();
        r.setId(a.getId());
        r.setFirstName(a.getFirstName());
        r.setLastName(a.getLastName());
        r.setAddress(a.getAddress());
        r.setAddress2(a.getAddress2());
        r.setArea(a.getArea());
        r.setState(a.getState());
        r.setCountry(a.getCountry());
        r.setZipCode(a.getZipCode());
        r.setPhoneNumber(a.getPhoneNumber());
        r.setDefault(a.isDefault());
        return r;
    }
}


