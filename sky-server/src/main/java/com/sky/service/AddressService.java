package com.sky.service;

import com.sky.entity.AddressBook;
import java.util.List;

public interface AddressService {
    void add(AddressBook addressBook);

    List<AddressBook> list();

    void setDefault(AddressBook addressBook);

    void update(AddressBook addressBook);

    AddressBook getById(Long id);

    AddressBook getDefault();

    void delete(Long id);
}
