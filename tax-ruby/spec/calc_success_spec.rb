# frozen_string_literal: true

require 'spec_helper'
require 'json'

RSpec.describe 'POST /tax/calc - success paths' do
  it 'returns 10% tax for USD' do
    payload = { amount: 100.0, currency: 'USD' }.to_json
    post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

    expect(last_response.status).to eq 200
    body = JSON.parse(last_response.body)
    expect(body['taxAmount']).to eq 10.0
    expect(body.keys).to eq(['taxAmount'])
  end

  it 'returns 8% tax for non-USD currency (e.g., BRL)' do
    payload = { amount: 100.0, currency: 'BRL' }.to_json
    post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

    expect(last_response.status).to eq 200
    body = JSON.parse(last_response.body)
    expect(body['taxAmount']).to eq 8.0
    expect(body.keys).to eq(['taxAmount'])
  end

  it 'is case-insensitive for currency (usd -> 10%)' do
    payload = { amount: 50.0, currency: 'usd' }.to_json
    post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

    expect(last_response.status).to eq 200
    body = JSON.parse(last_response.body)
    expect(body['taxAmount']).to eq 5.0
  end
end
