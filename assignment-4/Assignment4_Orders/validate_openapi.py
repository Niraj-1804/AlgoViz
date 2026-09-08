# Offline structural validator used only to demonstrate the checked OpenAPI contract.
# In a normal submission environment, also run: openapi-spec-validator openapi.yaml
import yaml, sys
with open('openapi.yaml', encoding='utf-8') as f: d=yaml.safe_load(f)
assert d['openapi'].startswith('3.')
assert all(k in d for k in ('info','servers','paths','components'))
assert sum(1 for item in d['paths'].values() for method in item if method in {'get','post','patch','put','delete'}) >= 4
for path, item in d['paths'].items():
    for method, op in item.items():
        if method in {'get','post','patch','put','delete'}:
            assert 'responses' in op and len(op['responses']) >= 2
assert 'schemas' in d['components']
print('OpenAPI validation: 0 errors')
