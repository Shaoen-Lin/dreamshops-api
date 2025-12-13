package com.Shawn.dream_shops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Blob;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // GenerationType.IDENTITY 交給 資料庫自動產生 (auto increment)
    // 且 刪除表裡的資料時，sequence 的值 不會自動重置。 下一筆新增資料會繼續從 sequence 目前的值 +1 開始，而不是從 1 開始。
    private Long id;
    private String fileName; // cat.jpg
    private String fileType; // .jpeg or .png

    @Lob
    private Blob image;
    // Blob (Binary Large Object) 表示大型二進制資料的資料類型。 它通常用來儲存圖片、音訊、影片等非結構化資料
    private String downloadUrl;

    @ManyToOne // means many images belong to one product
    @JoinColumn(name = "product_id") // Foreign key = 另一個表格加入的 PK 叫做 FK ( FK 要加再「多」的那邊 )
    @JsonIgnore
    private Product product;

}
