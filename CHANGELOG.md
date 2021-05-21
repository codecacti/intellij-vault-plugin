# Changelog

## [1.0.0]
### Added
- System Settings to persist authorization method and credentials
- Uses vault's HTTP endpoints to communicate to vault (except for `vault login`)
  - See vault's [lookup-a-token-self](https://www.vaultproject.io/api/auth/token#lookup-a-token-self) documentation
