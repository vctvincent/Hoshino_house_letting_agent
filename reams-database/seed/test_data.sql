USE reams;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 中介数据
-- agent_id: 6 ~ 9
-- =====================================================
INSERT INTO sys_agent (
    agent_id, agent_password, agent_name, agent_phone, agent_email,
    agent_avatar, agent_gender, agent_rating, agent_deal_count,
    agent_years_experience, agent_introduction, agent_company, agent_status
) VALUES
(6, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '张砚之', '13800138011', 'zhangyanzhi@reams.local', NULL, 1, 4.90, 2, 8, '擅长北京核心区改善盘与高流通二手房。', '北辰置业', 1),
(7, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '林语杉', '13800138012', 'linyushan@reams.local', NULL, 2, 4.75, 1, 6, '熟悉学区盘、科技园通勤盘与家庭改善需求。', '海岳地产', 1),
(8, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '周闻溪', '13800138013', 'zhouwenxi@reams.local', NULL, 1, 4.95, 2, 9, '长期服务上海浦东与静安高净值客户。', '沪上优居', 1),
(9, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '陈拓', '13800138014', 'chentuo@reams.local', NULL, 1, 4.70, 1, 5, '熟悉广州天河和珠江新城的地铁盘与商务盘。', '华南房策', 1);

-- =====================================================
-- 2. 客户数据
-- customer_id: 4 ~ 9
-- =====================================================
INSERT INTO sys_customer (
    customer_id, customer_password, customer_nickname, customer_phone,
    customer_email, customer_gender, customer_avatar, customer_status
) VALUES
(4, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '陈曦', '13900139011', 'chenxi@reams.local', 2, NULL, 1),
(5, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '何屹', '13900139012', 'heyi@reams.local', 1, NULL, 1),
(6, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '许宁', '13900139013', 'xuning@reams.local', 1, NULL, 1),
(7, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '万语', '13900139014', 'wanyu@reams.local', 2, NULL, 1),
(8, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '陆斐', '13900139015', 'lufei@reams.local', 1, NULL, 1),
(9, '$2a$10$XoLvF5C2dz9.7xOCLhOLlxdLqBvZuMJKwQ9N8K7Pz.FqGKJHmG', '秦墨', '13900139016', 'qinmo@reams.local', 1, NULL, 1);

-- =====================================================
-- 3. 房源数据
-- house_id: 11 ~ 22
-- 中介映射：1->6, 2->7, 3->8, 4->9
-- =====================================================
INSERT INTO h_house (
    house_id, agent_id, house_title, house_address, house_province, house_city, house_district, house_community,
    house_area, house_price, house_unit_price, house_type, house_layout, house_floor, house_total_floor,
    house_building_year, house_orientation, house_decoration, house_property_type, house_property_fee,
    house_elevator, house_heating, house_description, house_images, house_facilities, house_tags,
    house_status, house_view_count, house_favorite_count, house_audit_status, house_create_time
) VALUES
(11, 6, '国贸双景南向三居', '北京市朝阳区建国路88号', '北京市', '北京', '朝阳区', '双景公寓', 92.00, 980.00, 106521.74, '3室2厅2卫', '三居室', '18/28', 28, 2018, '南', '精装', '住宅', 5.80, 1, 1, '临近国贸商圈，通勤效率高，适合核心区改善型客户。', '["/uploads/houses/demo/real/living-room-photo-01.webp","/uploads/houses/demo/real/loft-living-01.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["地铁","商场","写字楼"]', 'CBD,改善盘,高流通', 2, 168, 1, 2, DATE_SUB(NOW(), INTERVAL 180 DAY)),
(12, 6, '望京地铁口景观两居', '北京市朝阳区阜通东大街18号', '北京市', '北京', '朝阳区', '望京花园', 79.00, 1120.00, 141772.15, '2室2厅1卫', '两居室', '10/20', 20, 2020, '东南', '精装', '住宅', 4.90, 1, 1, '地铁步行可达，适合年轻家庭与互联网从业者。', '["/uploads/houses/demo/real/living-room-photo-02.webp","/uploads/houses/demo/real/family-living-01.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["地铁","学校","商业街"]', '望京,地铁盘,改善', 1, 132, 2, 2, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(13, 7, '中关村学院路学区三居', '北京市海淀区学院路35号', '北京市', '北京', '海淀区', '知春家园', 105.00, 1380.00, 131428.57, '3室2厅2卫', '三居室', '6/18', 18, 2016, '南北', '简装', '住宅', 4.60, 1, 1, '兼顾学区与园区通勤，适合家庭自住。', '["/uploads/houses/demo/real/bedroom-photo-01.webp","/uploads/houses/demo/real/study-balcony-01.png","/uploads/houses/demo/real/garden-exterior-01.png"]', '["学校","地铁","医院"]', '学区,科技园,家庭', 2, 156, 1, 2, DATE_SUB(NOW(), INTERVAL 160 DAY)),
(14, 6, '东直门机场线精装一居', '北京市东城区东直门外大街46号', '北京市', '北京', '东城区', '东外公馆', 58.00, 720.00, 124137.93, '1室1厅1卫', '一居室', '12/22', 22, 2019, '西南', '精装', '公寓', 6.20, 1, 1, '适合自住或稳定出租，机场线与地铁双便利。', '["/uploads/houses/demo/real/bedroom-photo-02.webp","/uploads/houses/demo/real/bedroom-window-02.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["地铁","商圈","机场快线"]', '东城,通勤盘,投资', 2, 121, 0, 2, DATE_SUB(NOW(), INTERVAL 120 DAY)),
(15, 7, '丽泽金融区品质三居', '北京市丰台区丽泽路26号', '北京市', '北京', '丰台区', '丽泽时代', 97.00, 860.00, 88659.79, '3室2厅2卫', '三居室', '15/25', 25, 2021, '南北', '精装', '住宅', 4.20, 1, 1, '新金融区改善型房源，户型方正。', '["/uploads/houses/demo/real/living-room-photo-01.webp","/uploads/houses/demo/real/kitchen-dining-01.png","/uploads/houses/demo/real/garden-exterior-01.png"]', '["地铁","公园","商务区"]', '丰台,金融区,改善', 1, 89, 1, 2, DATE_SUB(NOW(), INTERVAL 75 DAY)),
(16, 8, '陆家嘴江景大平层', '上海市浦东新区银城中路600号', '上海市', '上海', '浦东新区', '滨江天际', 146.00, 1750.00, 119863.01, '4室2厅3卫', '四居室', '26/38', 38, 2017, '南', '豪装', '住宅', 8.20, 1, 0, '面向江景，适合高净值改善与商务客群。', '["/uploads/houses/demo/real/living-room-photo-02.webp","/uploads/houses/demo/real/loft-living-01.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["江景","会所","地铁"]', '浦东,江景,高端', 2, 188, 0, 2, DATE_SUB(NOW(), INTERVAL 140 DAY)),
(17, 8, '古美花园舒适三居', '上海市闵行区古美路899号', '上海市', '上海', '闵行区', '古美花园', 108.00, 998.00, 92407.41, '3室2厅2卫', '三居室', '8/16', 16, 2015, '南北', '精装', '住宅', 4.50, 1, 0, '成熟社区，适合改善型家庭。', '["/uploads/houses/demo/real/bedroom-photo-01.webp","/uploads/houses/demo/real/family-living-01.png","/uploads/houses/demo/real/garden-exterior-01.png"]', '["学校","社区商业","公园"]', '闵行,家庭盘,花园社区', 1, 96, 2, 2, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(18, 8, '南京西路轻奢两居', '上海市静安区南京西路1288号', '上海市', '上海', '静安区', '静安云邸', 88.00, 1920.00, 218181.82, '2室2厅1卫', '两居室', '19/30', 30, 2022, '南', '豪装', '公寓', 7.50, 1, 1, '核心商圈资产，适合城市高端置业。', '["/uploads/houses/demo/real/living-room-photo-01.webp","/uploads/houses/demo/real/bedroom-window-01.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["商圈","会所","地铁"]', '静安,高端盘,核心区', 2, 174, 0, 2, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(19, 7, '上地软件园通勤两居', '北京市海淀区上地十街3号', '北京市', '北京', '海淀区', '上地软件园家园', 86.00, 830.00, 96511.63, '2室2厅1卫', '两居室', '9/18', 18, 2018, '南北', '精装', '住宅', 4.10, 1, 1, '园区通勤友好，预算与流通性平衡。', '["/uploads/houses/demo/real/bedroom-photo-02.webp","/uploads/houses/demo/real/study-balcony-01.png","/uploads/houses/demo/real/garden-exterior-01.png"]', '["地铁","园区班车","超市"]', '海淀,通勤盘,性价比', 1, 111, 2, 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(20, 9, '珠江新城商务改善四居', '广州市天河区花城大道89号', '广东省', '广州', '天河区', '珠城国际', 138.00, 1520.00, 110144.93, '4室2厅2卫', '四居室', '21/33', 33, 2020, '南', '精装', '住宅', 5.90, 1, 0, '临近金融城和商务区，兼顾家庭居住与商务通达。', '["/uploads/houses/demo/real/living-room-photo-02.webp","/uploads/houses/demo/real/kitchen-dining-01.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["地铁","商务区","商场"]', '天河,商务盘,改善', 2, 149, 0, 2, DATE_SUB(NOW(), INTERVAL 65 DAY)),
(21, 8, '张江科学城精装三居', '上海市浦东新区盛夏路120号', '上海市', '上海', '浦东新区', '张江绿洲', 101.00, 1420.00, 140594.06, '3室2厅2卫', '三居室', '11/20', 20, 2021, '南北', '精装', '住宅', 5.10, 1, 1, '适合张江科学城产业人才与家庭客群。', '["/uploads/houses/demo/real/bedroom-photo-01.webp","/uploads/houses/demo/real/family-living-01.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["地铁","学校","科创园"]', '浦东,张江,科技盘', 1, 84, 1, 2, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(22, 6, '三里屯潮流小户型', '北京市朝阳区三里屯路19号', '北京市', '北京', '朝阳区', '三里屯里巷', 49.00, 590.00, 120408.16, '1室1厅1卫', '一居室', '7/16', 16, 2023, '东', '精装', '公寓', 6.80, 1, 1, '更适合年轻客群和短租资产配置。', '["/uploads/houses/demo/real/living-room-photo-01.webp","/uploads/houses/demo/real/bedroom-window-02.png","/uploads/houses/demo/real/tower-exterior-01.png"]', '["商圈","酒吧街","地铁"]', '朝阳,小户型,潮流', 0, 35, 1, 0, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- =====================================================
-- 4. 带看记录
-- viewing_id: 1 ~ 12
-- =====================================================
INSERT INTO h_viewing (
    viewing_id, customer_id, agent_id, house_id, viewing_appoint_time, viewing_actual_time,
    viewing_address, viewing_status, viewing_cancel_reason, viewing_cancel_by_type,
    viewing_cancel_by_id, viewing_remark, customer_phone, viewing_create_time
) VALUES
(1, 4, 6, 11, '2025-11-10 14:00:00', '2025-11-10 14:05:00', '北京市朝阳区建国路88号', 2, NULL, NULL, NULL, '客户重点关注通勤和学区。', '13900139011', '2025-11-08 10:00:00'),
(2, 5, 7, 13, '2025-12-05 10:30:00', '2025-12-05 10:35:00', '北京市海淀区学院路35号', 2, NULL, NULL, NULL, '客户预算充足，希望尽快成交。', '13900139012', '2025-12-02 09:20:00'),
(3, 6, 8, 16, '2026-01-14 15:00:00', '2026-01-14 15:10:00', '上海市浦东新区银城中路600号', 2, NULL, NULL, NULL, '客户重视景观和物业品质。', '13900139013', '2026-01-10 18:40:00'),
(4, 7, 6, 14, '2026-02-09 19:00:00', '2026-02-09 19:05:00', '北京市东城区东直门外大街46号', 2, NULL, NULL, NULL, '客户看重机场线与出租回报。', '13900139014', '2026-02-06 12:00:00'),
(5, 8, 8, 18, '2026-03-02 11:00:00', '2026-03-02 11:00:00', '上海市静安区南京西路1288号', 2, NULL, NULL, NULL, '客户偏爱核心商圈资产配置。', '13900139015', '2026-02-27 13:30:00'),
(6, 9, 9, 20, '2026-04-02 16:00:00', '2026-04-02 16:06:00', '广州市天河区花城大道89号', 2, NULL, NULL, NULL, '客户希望尽快完成贷款审批。', '13900139016', '2026-03-29 17:15:00'),
(7, 5, 6, 12, '2026-04-15 19:30:00', NULL, '北京市朝阳区阜通东大街18号', 1, NULL, NULL, NULL, '已确认，待带看。', '13900139012', '2026-04-12 09:15:00'),
(8, 7, 8, 21, '2026-04-08 14:30:00', '2026-04-08 14:35:00', '上海市浦东新区盛夏路120号', 2, NULL, NULL, NULL, '客户已进入签约准备阶段。', '13900139014', '2026-04-05 11:50:00'),
    (9, 4, 7, 15, '2026-04-05 13:30:00', NULL, '北京市丰台区丽泽路26号', 3, '客户临时调整购房计划', 3, 4, '本次带看取消，可重新预约。', '13900139011', '2026-04-03 16:20:00'),
(10, 8, 8, 17, '2026-04-10 10:00:00', '2026-04-10 10:10:00', '上海市闵行区古美路899号', 2, NULL, NULL, NULL, '客户比较满意，正在等待交易确认。', '13900139015', '2026-04-06 14:00:00'),
(11, 9, 6, 22, '2026-04-17 18:30:00', NULL, '北京市朝阳区三里屯路19号', 0, NULL, NULL, NULL, '房源待审核，预约暂未确认。', '13900139016', '2026-04-12 20:10:00'),
(12, 6, 7, 19, '2026-04-11 15:30:00', '2026-04-11 15:40:00', '北京市海淀区上地十街3号', 2, NULL, NULL, NULL, '客户关注性价比和后续议价空间。', '13900139013', '2026-04-08 08:45:00');

-- =====================================================
-- 5. 交易记录
-- transaction_id: 1 ~ 10
-- =====================================================
INSERT INTO h_transaction (
    transaction_id, house_id, customer_id, agent_id, viewing_id, transaction_no,
    transaction_final_price, transaction_deposit, transaction_payment_method, transaction_contract_url,
    transaction_deal_date, transaction_status, transaction_status_history, transaction_remark, transaction_create_time
) VALUES
(1, 11, 4, 6, 1, 'TX202511180001', 960.00, 50.00, '全款', NULL, '2025-11-18', 3, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2025-11-12 10:20:00"},{"from_status":1,"to_status":2,"remark":"签约完成","time":"2025-11-16 16:00:00"},{"from_status":2,"to_status":3,"remark":"完成过户","time":"2025-11-18 11:30:00"}]', '朝阳改善客户成交。', '2025-11-11 09:30:00'),
(2, 13, 5, 7, 2, 'TX202512120001', 1320.00, 80.00, '贷款', NULL, '2025-12-12', 3, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2025-12-06 14:10:00"},{"from_status":1,"to_status":2,"remark":"签约完成","time":"2025-12-10 18:10:00"},{"from_status":2,"to_status":3,"remark":"贷款发放","time":"2025-12-12 15:20:00"}]', '海淀学区盘完成成交。', '2025-12-05 12:10:00'),
(3, 16, 6, 8, 3, 'TX202601220001', 1680.00, 120.00, '贷款', NULL, '2026-01-22', 3, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2026-01-15 10:00:00"},{"from_status":1,"to_status":2,"remark":"签约完成","time":"2026-01-20 14:00:00"},{"from_status":2,"to_status":3,"remark":"完成付款","time":"2026-01-22 11:40:00"}]', '浦东高端盘成交。', '2026-01-14 19:20:00'),
(4, 14, 7, 6, 4, 'TX202602160001', 710.00, 40.00, '全款', NULL, '2026-02-16', 3, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2026-02-10 09:30:00"},{"from_status":1,"to_status":2,"remark":"签约完成","time":"2026-02-14 17:50:00"},{"from_status":2,"to_status":3,"remark":"完成交割","time":"2026-02-16 13:15:00"}]', '东城小户型投资成交。', '2026-02-09 21:10:00'),
(5, 18, 8, 8, 5, 'TX202603080001', 1880.00, 150.00, '分期', NULL, '2026-03-08', 3, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2026-03-03 09:00:00"},{"from_status":1,"to_status":2,"remark":"签约完成","time":"2026-03-06 15:30:00"},{"from_status":2,"to_status":3,"remark":"完成付款","time":"2026-03-08 16:20:00"}]', '静安核心区资产配置成交。', '2026-03-02 16:10:00'),
(6, 20, 9, 9, 6, 'TX202604060001', 1460.00, 100.00, '贷款', NULL, '2026-04-06', 3, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2026-04-03 09:20:00"},{"from_status":1,"to_status":2,"remark":"签约完成","time":"2026-04-05 12:30:00"},{"from_status":2,"to_status":3,"remark":"贷款放款","time":"2026-04-06 18:00:00"}]', '天河商务改善盘成交。', '2026-04-02 18:00:00'),
(7, 12, 5, 6, 7, 'TX202604120001', 1120.00, 60.00, '贷款', NULL, NULL, 1, '[{"from_status":0,"to_status":1,"remark":"客户确认看房后进入谈判","time":"2026-04-12 19:30:00"}]', '望京房源正在谈判中。', '2026-04-12 19:30:00'),
(8, 21, 7, 8, 8, 'TX202604090001', 1390.00, 90.00, '分期', NULL, NULL, 2, '[{"from_status":0,"to_status":1,"remark":"客户确认","time":"2026-04-08 19:00:00"},{"from_status":1,"to_status":2,"remark":"合同已上传","time":"2026-04-09 14:20:00"}]', '张江房源已签约待完成。', '2026-04-08 18:10:00'),
(9, 15, 4, 7, 9, 'TX202604050001', 820.00, 30.00, '全款', NULL, NULL, 4, '[{"from_status":0,"to_status":4,"remark":"客户调整计划","time":"2026-04-05 15:00:00"}]', '客户临时放弃，交易取消。', '2026-04-05 10:20:00'),
(10, 17, 8, 8, 10, 'TX202604100001', 980.00, 50.00, '贷款', NULL, NULL, 0, '[]', '闵行房源待客户正式确认。', '2026-04-10 11:30:00');

-- =====================================================
-- 6. 收藏记录
-- favorite_id: 2 ~ 11
-- =====================================================
INSERT INTO h_favorite (favorite_id, customer_id, house_id, favorite_create_time) VALUES
(2, 4, 12, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, 5, 17, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 5, 19, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5, 6, 21, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 7, 15, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7, 8, 17, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 8, 22, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 9, 13, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(10, 4, 19, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 7, 12, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 7. 评价记录
-- review_id: 5 ~ 12
-- =====================================================
INSERT INTO h_review (
    review_id, transaction_id, viewing_id, house_id, agent_id, customer_id,
    review_target_type, review_rating, review_content, review_is_show, review_create_time
) VALUES
(5, NULL, 1, 11, 6, 4, 2, 5, '带看节奏很稳，讲解专业，后续跟进也很及时。', 1, DATE_SUB(NOW(), INTERVAL 145 DAY)),
(6, NULL, NULL, 12, 6, 5, 1, 4, '房源整体不错，地铁和商业配套都很方便。', 1, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(7, NULL, 3, 16, 8, 6, 2, 5, '服务细致，能把价格和板块逻辑讲得很清楚。', 1, DATE_SUB(NOW(), INTERVAL 80 DAY)),
(8, NULL, NULL, 18, 8, 8, 1, 5, '核心地段保值性强，房源状态也很好。', 1, DATE_SUB(NOW(), INTERVAL 34 DAY)),
(9, NULL, 4, 14, 6, 7, 2, 4, '沟通顺畅，安排时间灵活。', 1, DATE_SUB(NOW(), INTERVAL 56 DAY)),
(10, NULL, 12, 19, 7, 6, 2, 4, '性价比分析到位，适合预算型客户参考。', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, NULL, NULL, 19, 7, 6, 1, 4, '户型实用，适合作为通勤盘备选。', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(12, NULL, 6, 20, 9, 9, 2, 5, '贷款流程说明清楚，推进效率高。', 1, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- =====================================================
-- 8. 消息记录
-- message_id: 48 ~ 55
-- sender_type / receiver_type:
-- 1-管理员 2-中介 3-客户
-- =====================================================
INSERT INTO h_message (
    message_id, message_sender_id, message_sender_type, message_receiver_id, message_receiver_type,
    message_type, message_scene, message_title, message_content, message_house_id, message_viewing_id,
    message_is_read, message_create_time
) VALUES
(48, 5, 3, 6, 2, 2, 'VIEWING_REQUEST', '新的带看申请', '客户何屹申请预约望京地铁口景观两居。', 12, 7, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(49, 8, 2, 8, 3, 1, 'VIEWING_REVIEW_INVITE', '带看已完成，请评价本次服务', '古美花园舒适三居已完成带看，请对本次中介服务进行评分。', 17, 10, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(50, 6, 2, 5, 3, 1, 'TRANSACTION_CREATED', '交易已创建', '望京地铁口景观两居已进入交易谈判阶段，请及时确认。', 12, 7, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(51, 6, 3, 7, 2, 2, 'CHAT', '咨询上地房源', '这套上地两居后续还有多少议价空间？', 19, 12, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(52, 8, 2, 7, 3, 1, 'TRANSACTION_CREATED', '交易创建提醒', '张江科学城精装三居已创建交易，当前状态为已签约。', 21, 8, 0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(53, 7, 2, 6, 3, 1, 'VIEWING_REVIEW_INVITE', '带看完成评价邀请', '上地软件园通勤两居带看已结束，欢迎留下服务评价。', 19, 12, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(54, 1, 1, 7, 2, 1, 'HOUSE_AUDIT_APPROVED', '房源审核通过', '上地软件园通勤两居已通过审核并可正常展示。', 19, NULL, 1, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(55, 1, 1, 7, 2, 1, 'CHAT', '看板提示', '朝阳区近一个月客户咨询活跃，建议尽快补充在架房源。', 12, NULL, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- =====================================================
-- 9. 审核记录
-- audit_id: 11 ~ 22
-- 这里按房源状态补 12 条审核数据
-- admin_id 默认使用 1
-- =====================================================
INSERT INTO h_audit (
    audit_id, house_id, admin_id, audit_type, audit_result, audit_reason, audit_time, audit_create_time
) VALUES
(11, 11, 1, 1, 2, '房源信息完整，审核通过。', DATE_SUB(NOW(), INTERVAL 179 DAY), DATE_SUB(NOW(), INTERVAL 179 DAY)),
(12, 12, 1, 1, 2, '房源图片清晰，审核通过。', DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),
(13, 13, 1, 1, 2, '学区盘信息有效，审核通过。', DATE_SUB(NOW(), INTERVAL 159 DAY), DATE_SUB(NOW(), INTERVAL 159 DAY)),
(14, 14, 1, 1, 2, '房源信息规范，审核通过。', DATE_SUB(NOW(), INTERVAL 119 DAY), DATE_SUB(NOW(), INTERVAL 119 DAY)),
(15, 15, 1, 1, 2, '信息真实有效，审核通过。', DATE_SUB(NOW(), INTERVAL 74 DAY), DATE_SUB(NOW(), INTERVAL 74 DAY)),
(16, 16, 1, 1, 2, '高端房源资料齐全，审核通过。', DATE_SUB(NOW(), INTERVAL 139 DAY), DATE_SUB(NOW(), INTERVAL 139 DAY)),
(17, 17, 1, 1, 2, '社区房源资料完整，审核通过。', DATE_SUB(NOW(), INTERVAL 44 DAY), DATE_SUB(NOW(), INTERVAL 44 DAY)),
(18, 18, 1, 1, 2, '核心区房源审核通过。', DATE_SUB(NOW(), INTERVAL 89 DAY), DATE_SUB(NOW(), INTERVAL 89 DAY)),
(19, 19, 1, 1, 2, '房源审核通过，允许展示。', DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY)),
(20, 20, 1, 1, 2, '商务改善盘资料齐全，审核通过。', DATE_SUB(NOW(), INTERVAL 64 DAY), DATE_SUB(NOW(), INTERVAL 64 DAY)),
(21, 21, 1, 1, 2, '张江房源审核通过。', DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
(22, 22, 1, 1, 3, '房源仍在补充资料，暂未通过审核。', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY));

-- =====================================================
-- 10. 调整自增起点（下一条从后续开始）
-- =====================================================
ALTER TABLE sys_agent AUTO_INCREMENT = 10;
ALTER TABLE sys_customer AUTO_INCREMENT = 10;
ALTER TABLE h_house AUTO_INCREMENT = 23;
ALTER TABLE h_viewing AUTO_INCREMENT = 13;
ALTER TABLE h_transaction AUTO_INCREMENT = 11;
ALTER TABLE h_favorite AUTO_INCREMENT = 12;
ALTER TABLE h_review AUTO_INCREMENT = 13;
ALTER TABLE h_message AUTO_INCREMENT = 56;
ALTER TABLE h_audit AUTO_INCREMENT = 23;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ 追加式测试数据插入完成' AS result;