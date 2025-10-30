package com.generic.rest.main.controller;

import com.generic.rest.main.dto.AddressDTO;
import com.generic.rest.main.model.User;
import com.generic.rest.main.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<AddressDTO>> list(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.listAddresses(user));
    }

    @PostMapping("/create")
    public ResponseEntity<AddressDTO> create(Authentication authentication, @Valid @RequestBody AddressDTO request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.createAddress(user, request));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<AddressDTO> edit(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody AddressDTO req) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.editAddress(user, id, req));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        addressService.deleteAddress(user, id);
        return ResponseEntity.noContent().build();
    }
}


