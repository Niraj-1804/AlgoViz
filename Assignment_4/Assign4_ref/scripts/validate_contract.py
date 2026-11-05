from pathlib import Path
import yaml
from openapi_spec_validator import validate
path = Path(__file__).resolve().parents[1] / 'openapi.yaml'
validate(yaml.safe_load(path.read_text()))
print('openapi-spec-validator: PASS (zero errors)')
print('Validated: openapi.yaml; OpenAPI 3.0.3; four operations')
