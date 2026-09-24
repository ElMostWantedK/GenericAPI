package com.generic.rest.main.repository;

import com.generic.rest.main.model.Address;
import com.generic.rest.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserOrderByIdAsc(User user);

    long countByUser(User user);

    Optional<Address> findByIdAndUser(Long id, User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);
}
