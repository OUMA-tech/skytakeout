package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImp implements AddressService {
    @Autowired
    private AddressMapper addressMapper;
    @Override
    public void add(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressMapper.add(addressBook);
    }

    @Override
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> addressBookList = addressMapper.list(userId);
        return addressBookList;
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressMapper.updateIsDefaultByUserId(addressBook);

        addressBook.setIsDefault(1);
        addressMapper.update(addressBook);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressMapper.update(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressMapper.getById(id);
        return addressBook;
    }

    @Override
    public AddressBook getDefault() {
        Long userId = BaseContext.getCurrentId();
        return addressMapper.getDefault(userId);
    }

    @Override
    public void delete(Long id) {
        addressMapper.deleteBatch(id);
    }


}
