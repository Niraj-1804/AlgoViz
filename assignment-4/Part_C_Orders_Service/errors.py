from flask import jsonify


def problem(status, title, detail, problem_type=None):
    """Return the single error representation used by every endpoint."""
    body = {
        "type": problem_type or f"https://example.com/problems/{status}",
        "title": title,
        "status": status,
        "detail": detail,
    }
    return jsonify(body), status
