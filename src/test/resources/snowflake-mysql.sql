CREATE TABLE id_generator(
  biz_tag VARCHAR NOT NULL  COMMENT '业务名',
  max_id  BIGINT NOT NULL DEFAULT 1 COMMENT '当前最大id值',
  step INT(20) DEFAULT 100 COMMENT '批量取ID增加的步长',
  biz_describe VARCHAR(256) COMMENT '描述',
  update_time DATETIME
);

INSERT INTO id_generator(biz_tag, biz_describe, update_time) VALUES ('test', '单元测试', CURRENT_DATE);