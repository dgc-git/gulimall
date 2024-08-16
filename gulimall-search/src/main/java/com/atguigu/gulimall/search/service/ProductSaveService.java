package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author dgc
 * @date 2024/8/16 8:52
 */
@Service
public interface ProductSaveService {

    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
