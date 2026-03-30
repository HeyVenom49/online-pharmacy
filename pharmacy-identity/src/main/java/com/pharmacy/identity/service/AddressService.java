package com.pharmacy.identity.service;

import com.pharmacy.common.exception.ResourceNotFoundException;
import com.pharmacy.identity.dto.AddressDTO;
import com.pharmacy.identity.dto.AddressRequest;
import com.pharmacy.identity.entity.Address;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.repository.AddressRepository;
import com.pharmacy.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressDTO> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }

    public AddressDTO getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address", addressId);
        }

        return toAddressDTO(address);
    }

    @Transactional
    public AddressDTO createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = Address.builder()
                .user(user)
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        Address savedAddress = addressRepository.save(address);
        return toAddressDTO(savedAddress);
    }

    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address", addressId);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
        }

        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        Address updatedAddress = addressRepository.save(address);
        return toAddressDTO(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address", addressId);
        }

        addressRepository.delete(address);
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address", addressId);
        }

        addressRepository.clearDefaultForUser(userId);
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        return toAddressDTO(updatedAddress);
    }

    private AddressDTO toAddressDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .isDefault(address.getIsDefault())
                .build();
    }
}
