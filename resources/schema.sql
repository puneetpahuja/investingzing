-- Drop tables
DROP TABLE IF EXISTS mf_nav;
DROP TABLE IF EXISTS mf_scheme;

-- Drop types
DROP TYPE IF EXISTS datasource;

-- Create types
CREATE TYPE datasource AS ENUM ('api_less', 'api_more', 'navall');

-- Create schema
CREATE TABLE mf_scheme (
  code INT NOT NULL PRIMARY KEY,
  isin_div_payout_growth TEXT,
  isin_div_reinvestment TEXT,
  "name" TEXT NOT NULL,
  house TEXT,
  category TEXT,
  "type" TEXT,
  source datasource NOT NULL
);

CREATE TABLE mf_nav (
  code INT NOT NULL REFERENCES mf_scheme(code),
  "date" DATE NOT NULL,
  nav DOUBLE PRECISION NOT NULL,
  PRIMARY KEY (code, "date", nav)
);
