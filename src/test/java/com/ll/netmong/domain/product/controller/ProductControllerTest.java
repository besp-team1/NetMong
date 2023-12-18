package com.ll.netmong.domain.product.controller;

import com.ll.netmong.common.RsData;
import com.ll.netmong.domain.product.dto.request.CreateRequest;
import com.ll.netmong.domain.product.service.ProductServiceImpl;
import com.ll.netmong.domain.product.util.Category;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductControllerTest {
    @Mock
    private ProductServiceImpl productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

//    @DisplayName("상품 등록 확인")
//    @Test
//    void post_Product() throws IOException {
//        CreateRequest createRequest = productRequest();
//        RsData productResponse = productResponse();
//
//
//    }

    private CreateRequest productRequest() {
        CreateRequest createRequest = new CreateRequest();
        createRequest.setProductName("강아지 사료");
        createRequest.setPrice("25_000");
        createRequest.setContent("유통기한 1년 남았습니다.");
        createRequest.setCategory(Category.PET_FEED);

        return createRequest;
    }

    private RsData productResponse() {
        return RsData.of("S-1", "상품이 등록 되었습니다.", "create");
    }
}
