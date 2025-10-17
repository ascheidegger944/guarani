@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        OrderRequest request = OrderRequest.builder().productId(1L).quantity(2).build();
        Product product = Product.builder().id(1L).price(BigDecimal.TEN).build();

        // When
        when(productService.findById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(Order.builder().id(1L).build());

        OrderResponse response = orderService.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }
}