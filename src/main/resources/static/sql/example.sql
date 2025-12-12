-- 1. 권한 (Role)
CREATE TABLE roles (
    role_name VARCHAR(50) PRIMARY KEY -- 예: 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER'
);

/* ============================================================
   1. 권한 (Roles)
   - 관리자(ADMIN), 담당자(MANAGER), 일반사용자(USER)
   ============================================================ */
INSERT INTO roles (role_name) VALUES
('ROLE_ADMIN'),   -- 시스템 전체 관리자 (모든 권한 제어 가능)
('ROLE_MANAGER'), -- 시설 담당자 (자산 데이터 관리, 일반 유저 권한 제어 가능)
('ROLE_USER');    -- 일반 조회 사용자


-- 2. 사용자 (User)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL, -- 실제론 암호화 저장
    role_name VARCHAR(50) REFERENCES roles(role_name) -- 사용자는 하나의 권한을 가짐 (단순화)
);

/* ============================================================
   2. 사용자 (Users)
   - 비밀번호는 편의상 평문으로 넣었으나, 실제 앱에선 BCrypt 암호화 필요
   ============================================================ */
INSERT INTO users (username, password, role_name) VALUES
('admin', '1234', 'ROLE_ADMIN'),     -- 관리자: 모든 데이터 조회 및 설정 가능
('manager', '1234', 'ROLE_MANAGER'), -- 담당자: 비용 정보 조회 가능
('user', '1234', 'ROLE_USER');       -- 일반유저: 민감 정보(비용, 계약) 조회 불가


-- 3. [Grid Table] 그리드 마스터 (모든 컬럼 정의 저장)
CREATE TABLE grid_master (
    grid_code VARCHAR(50) PRIMARY KEY,  -- 예: 'FACILITY_ASSET_GRID' (식별자)
    grid_name VARCHAR(100) NOT NULL,    -- 예: '시설 자산 관리 대장'
    -- [핵심] 해당 그리드가 가진 '모든' 컬럼 정보 (컬럼명, 타입, 헤더명 등)
    -- 예: [{"field": "purchaseCost", "header": "구매가", "type": "number"}, ...]
    default_columns_json JSONB NOT NULL
);

/* ============================================================
   3. 그리드 마스터 (Grid Master)
   - 시설 자산 관리 대장(FACILITY_ASSET_GRID)의 '모든' 컬럼 정의
   ============================================================ */
INSERT INTO grid_master (grid_code, grid_name, default_columns_json) VALUES
('FACILITY_ASSET_GRID', '시설 자산 관리 대장',
'[
{"field": "id", "header": "No", "width": 50, "type": "number", "align": "center"},
{"field": "assetCode", "header": "자산코드", "width": 100, "type": "string", "align": "center"},
{"field": "assetName", "header": "자산명", "width": 200, "type": "string", "align": "left"},
{"field": "category", "header": "분류", "width": 100, "type": "string", "align": "center"},
{"field": "location", "header": "설치위치", "width": 150, "type": "string", "align": "left"},
{"field": "modelName", "header": "모델명", "width": 150, "type": "string", "align": "left"},
{"field": "manufacturer", "header": "제조사", "width": 120, "type": "string", "align": "center"},
{"field": "status", "header": "상태", "width": 80, "type": "string", "align": "center"},
{"field": "installDate", "header": "설치일", "width": 100, "type": "date", "align": "center"},
{"field": "purchaseCost", "header": "구매비용", "width": 120, "type": "number", "align": "right"},
{"field": "contractDetails", "header": "계약내용", "width": 250, "type": "string", "align": "left"}
]'::jsonb);


-- 4. [Permission Grid Table] 권한별 설정 (볼 수 '없는' 컬럼 저장)
CREATE TABLE grid_role_config (
    id SERIAL PRIMARY KEY,
    grid_code VARCHAR(50) REFERENCES grid_master(grid_code),
    role_name VARCHAR(50) REFERENCES roles(role_name),
    -- [핵심] 해당 권한에서 '제외할' 컬럼 리스트
    -- 보안 로직: 이 JSON에 포함된 필드는 서버에서 아예 내려주지 않음
    -- 예: ["purchaseCost", "contractDetails"]
    invisible_columns_json JSONB DEFAULT '[]'::jsonb,
    UNIQUE(grid_code, role_name) -- 한 그리드에 대해 권한당 하나의 설정만 존재
);

/* ============================================================
   4. 권한별 그리드 설정 (Grid Role Config)
   - 핵심: 권한에 따라 서버에서 아예 내려주지 않을 컬럼 정의
   ============================================================ */
-- A. 관리자 (ADMIN): 제한 없음 (빈 배열)
INSERT INTO grid_role_config (grid_code, role_name, invisible_columns_json) VALUES
('FACILITY_ASSET_GRID', 'ROLE_ADMIN', '[]'::jsonb);

-- B. 담당자 (MANAGER): 제한 없음 (일단 관리자와 동일하게 모든 정보 접근 허용)
INSERT INTO grid_role_config (grid_code, role_name, invisible_columns_json) VALUES
('FACILITY_ASSET_GRID', 'ROLE_MANAGER', '[]'::jsonb);

-- C. 일반 사용자 (USER): 민감 정보(구매비용, 계약내용) 조회 불가
INSERT INTO grid_role_config (grid_code, role_name, invisible_columns_json) VALUES
('FACILITY_ASSET_GRID', 'ROLE_USER', '["purchaseCost", "contractDetails"]'::jsonb);


-- 5. [User Grid Table] 사용자 개인 설정 (숨김, 너비 등)
CREATE TABLE grid_user_config (
    id SERIAL PRIMARY KEY,
    grid_code VARCHAR(50) REFERENCES grid_master(grid_code),
    user_id INTEGER REFERENCES users(id),
    -- [핵심] 사용자의 개인화 설정 (숨김 컬럼, 컬럼 순서, 너비 등 복합 정보)
    -- 예: { "hidden": ["modelName"], "widths": {"assetName": 200}, "order": [...] }
    config_json JSONB DEFAULT '{}'::jsonb,
    UNIQUE(grid_code, user_id) -- 한 그리드에 대해 유저당 하나의 설정만 존재
);

/* ============================================================
   5. 사용자 개인 설정 (Grid User Config)
   - 사용자가 UI에서 "보기 싫다"고 설정한 내용 (Hidden, Width 등)
   ============================================================ */
-- 상황: 'user'(일반유저)는 화면이 좁아서 'ID', '모델명', '제조사'를 숨기고 싶어함.
--      그리고 '자산명'을 좀 더 넓게(300px) 보고 싶어함.
INSERT INTO grid_user_config (grid_code, user_id, config_json) VALUES
('FACILITY_ASSET_GRID', (SELECT id FROM users WHERE username='user'),
 '{
   "hiddenColumns": ["id", "modelName", "manufacturer"],
   "columnWidths": {
     "assetName": 300
   }
 }'::jsonb);


-- 5. [Facility Asset Table] 비즈니스 데이터
CREATE TABLE facility_asset (
                                id SERIAL PRIMARY KEY,
                                asset_code VARCHAR(50) NOT NULL UNIQUE,  -- 자산 번호 (예: HVAC-001)
                                asset_name VARCHAR(100) NOT NULL,        -- 자산 명칭
                                category VARCHAR(50),                    -- 분류 (기계, 전기, 소방 등)
                                location VARCHAR(100),                   -- 설치 위치
                                model_name VARCHAR(100),                 -- 모델명
                                manufacturer VARCHAR(100),               -- 제조사
                                install_date DATE,                       -- 설치일
                                status VARCHAR(20),                      -- 상태 (정상, 고장, 점검중)
                                purchase_cost NUMERIC(15, 2),            -- 구매 비용 (민감 정보 - 권한 필요)
                                contract_details TEXT                    -- 계약 내용 (민감 정보 - 권한 필요)
);

/* ============================================================
   6. 비즈니스 데이터 (Facility Asset)
   ============================================================ */
INSERT INTO facility_asset (asset_code, asset_name, category, location, model_name, manufacturer, install_date, status, purchase_cost, contract_details) VALUES
('HVAC-001', '메인 칠러 1호기', '기계설비', 'B2 기계실', 'RT-1000', 'LG전자', '2020-05-15', '정상', 150000000, '유지보수 계약: A사 (월 50만원)'),
('ELEC-102', '수배전반 2호', '전기설비', 'B1 전기실', 'PN-2023', 'LS일렉트릭', '2019-11-20', '점검중', 45000000, '하자보수 기간 만료됨'),
('FIRE-005', 'R형 수신기', '소방설비', '1F 방재실', 'FP-500', '동방전자', '2021-03-10', '정상', 12000000, '연 2회 정밀점검 필수'),
('LIFT-001', '승객용 엘리베이터 1호', '승강기', '로비', 'WB-Speed', '현대엘리베이터', '2018-08-01', '정상', 85000000, 'FMS 유지보수 계약 체결'),
('SEC-CAM-22', '지하주차장 CCTV', '보안설비', 'B3 주차장', 'Vision-X', '한화테크윈', '2022-01-15', '고장', 500000, '렌탈 계약 상품'),
('PUMP-003', '급수 펌프 3호', '기계설비', 'B2 펌프실', 'Grundfos-X', '그런포스', '2020-06-20', '정상', 3500000, '소모품 교체 주기: 1년'),
('HVAC-002', '공조기 2호기', '기계설비', '옥상층', 'AHU-2000', '귀뚜라미', '2021-07-01', '정상', 25000000, '무상 보증 기간 내'),
('NET-SW-01', '메인 스위치 허브', '통신설비', 'MDF실', 'Cisco-9200', 'Cisco', '2023-01-10', '정상', 4500000, '네트워크 유지보수 포함'),
('GEN-001', '비상발전기', '전기설비', '별관 발전기실', 'Doosan-V12', '두산밥캣', '2015-04-12', '정상', 80000000, '매월 무부하 운전 실시'),
('AUTO-DR-01', '주차장 자동문', '건축설비', '주차장 입구', 'Speed-Door', 'KONE', '2022-09-09', '수리중', 8000000, '센서 오작동 빈번');




-- 1. 성능 테스트용 대량 데이터 생성 함수
DO $$
DECLARE
i INT;
BEGIN
    -- 기존 데이터 10만 건 생성 (generate_series 이용)
    -- 실제 컬럼은 10개지만, 로직 부하를 테스트하기엔 충분합니다.
INSERT INTO facility_asset (asset_code, asset_name, category, location, model_name, manufacturer, install_date, status, purchase_cost, contract_details)
SELECT
    'TEST-ASSET-' || gs,
    '성능 테스트 자산 ' || gs,
    CASE WHEN gs % 3 = 0 THEN '기계' WHEN gs % 3 = 1 THEN '전기' ELSE '소방' END,
    'B' || (gs % 5) || '층',
    'MODEL-' || gs,
    'Manufacturer-' || (gs % 10),
    CURRENT_DATE - (gs % 3650), -- 최근 10년 내 랜덤 날짜
    CASE WHEN gs % 10 = 0 THEN '고장' ELSE '정상' END,
    (gs * 1000)::numeric,
    '계약 내용 상세 데이터입니다. 길이가 길어질수록 메모리를 많이 차지합니다...' || gs
FROM generate_series(1, 100000) AS gs; -- 10만 건 생성
END $$;