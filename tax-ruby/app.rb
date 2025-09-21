require 'sinatra'
require 'json'

set :bind, '0.0.0.0'
set :port, 4567

# POST /tax/calc
# Request JSON: { amount, currency, userId }
# Response JSON: { taxAmount }
post '/tax/calc' do
  content_type :json
  begin
    payload = JSON.parse(request.body.read)
    amount = payload['amount']
    currency = payload['currency']

    halt 400, { error: 'amount is required' }.to_json unless amount
    halt 400, { error: 'currency is required' }.to_json unless currency

    # Simple tax rule: 10% for USD, 8% for others
    rate = currency == 'USD' ? 0.10 : 0.08
    tax_amount = (amount.to_f * rate).round(2)

    status 200
    { taxAmount: tax_amount }.to_json
  rescue JSON::ParserError
    halt 400, { error: 'invalid JSON' }.to_json
  end
end

get '/health' do
  content_type :json
  { status: 'ok', time: Time.now.to_i }.to_json
end
