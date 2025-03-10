package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "user - address book")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    @ApiOperation("add address")
    public Result add(@RequestBody AddressBook addressBook) {
        log.info("add address");
        addressService.add(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("get address list")
    public Result<List<AddressBook>> list() {
        log.info("get address list");
        return Result.success(addressService.list());
    }

    @PutMapping("/default")
    @ApiOperation("set default address")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        log.info("set default address:{}", addressBook);
        addressService.setDefault(addressBook);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("update address")
    public Result update(@RequestBody AddressBook addressBook) {
        log.info("update address:{}", addressBook);
        addressService.update(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("get address by id")
    public Result<AddressBook> getById(@PathVariable Long id) {
        log.info("get address by id:{}", id);
        AddressBook addressBook = addressService.getById(id);
        return Result.success(addressBook);
    }

    @GetMapping("/default")
    @ApiOperation("get default address")
    public Result<AddressBook> getDefault() {
        log.info("get default address");
        AddressBook addressBook = addressService.getDefault();
        return Result.success(addressBook);
    }

    @DeleteMapping
    @ApiOperation("delete address")
    public Result delete(@RequestParam Long id) {
        log.info("delete address:{}", id);
        addressService.delete(id);
        return Result.success();
    }
}
