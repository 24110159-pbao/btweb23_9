package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(
            target = "username",
            source = "user.username"
    )
    ProductDTO toDTO(Product product);

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "user",
            ignore = true
    )
    Product toEntity(ProductDTO dto);

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "user",
            ignore = true
    )
    @Mapping(
            target = "imageUrl",
            ignore = true
    )
    @Mapping(
            target = "imagePublicId",
            ignore = true
    )
    void updateEntity(
            ProductDTO dto,
            @MappingTarget Product product
    );
}