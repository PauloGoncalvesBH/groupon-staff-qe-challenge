require 'sinatra'
require 'json'

set :bind, '0.0.0.0'
set :port, 4567

# POST /tax/calc
# Request JSON: { amount, currency }
# Response JSON: { taxAmount }
post '/tax/calc' do
  content_type :json
  begin
    payload = JSON.parse(request.body.read)
  rescue JSON::ParserError
    halt 400, { error: 'invalid JSON' }.to_json
  end

  amount = payload['amount']
  currency = payload['currency']

  halt 400, { error: 'amount is required' }.to_json if amount.nil?
  halt 400, { error: 'currency is required' }.to_json if currency.nil?

  unless amount.is_a?(Numeric) && amount.finite?
    halt 400,({ error: 'amount must be a number' }.to_json)
  end

  unless currency.is_a?(String) && currency.match?(/\A[a-zA-Z]{3}\z/)
    halt 400,({ error: 'currency must be a 3-letter string' }.to_json)
  end

  # Simple tax rule: 10% for USD, 8% for others
  rate = currency.to_s.upcase == 'USD' ? 0.10 : 0.08
  tax_amount = (amount.to_f * rate).round(2)

  status 200
  { taxAmount: tax_amount }.to_json
end

get '/health' do
  content_type :json
  { status: 'ok', time: Time.now.to_i }.to_json
end
