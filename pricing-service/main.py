from decimal import Decimal

from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(
    title="OrderFlow Pricing Service",
    version="1.0.0"
)


class PricingRequest(BaseModel):
    instrument_id: str
    side: str
    quantity: int = Field(gt=0)
    reference_price: Decimal = Field(gt=0)


class PricingResponse(BaseModel):
    instrument_id: str
    side: str
    quantity: int
    reference_price: Decimal
    calculated_price: Decimal


@app.get("/health")
async def health():
    return {
        "status": "UP",
        "service": "pricing-service"
    }


@app.post("/api/pricing", response_model=PricingResponse)
async def calculate_price(request: PricingRequest):

    adjustment = Decimal("0.001")

    if request.side.upper() == "BUY":
        calculated_price = (
            request.reference_price
            * (Decimal("1") + adjustment)
        )
    else:
        calculated_price = (
            request.reference_price
            * (Decimal("1") - adjustment)
        )

    calculated_price = calculated_price.quantize(
        Decimal("0.0001")
    )

    return PricingResponse(
        instrument_id=request.instrument_id,
        side=request.side,
        quantity=request.quantity,
        reference_price=request.reference_price,
        calculated_price=calculated_price
    )