package com.sky.service;

import com.sky.entity.AddressBook;
import java.util.List;

public interface AddressBookService {

    void save(AddressBook addressBook);

    void deleteById(Long id);

    void update(AddressBook addressBook);

    void setDefault(AddressBook addressBook);

    List<AddressBook> list(AddressBook addressBook);

    AddressBook getById(Long id);
}
