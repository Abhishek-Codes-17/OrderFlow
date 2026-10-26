from fastapi.testclient import TestClient

from main import app


client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")

    assert response.status_code == 200

    assert response.json() == {
        "status": "UP",
        "service": "pricing-service"
    }


def test_buy_price():
    response = client.post(
        "/api/pricing",
        json={
            "instrument_id": "AAPL",
            "side": "BUY",
            "quantity": 100,
            "reference_price": 200.00
        }
    )

    assert response.status_code == 200

    body = response.json()

    assert body["instrument_id"] == "AAPL"
    assert body["side"] == "BUY"
    assert body["quantity"] == 100
    assert body["calculated_price"] == "200.2000"


def test_sell_price():
    response = client.post(
        "/api/pricing",
        json={
            "instrument_id": "AAPL",
            "side": "SELL",
            "quantity": 100,
            "reference_price": 200.00
        }
    )

    assert response.status_code == 200

    body = response.json()

    assert body["calculated_price"] == "199.8000"


def test_reject_invalid_quantity():
    response = client.post(
        "/api/pricing",
        json={
            "instrument_id": "AAPL",
            "side": "BUY",
            "quantity": 0,
            "reference_price": 200.00
        }
    )

    assert response.status_code == 422