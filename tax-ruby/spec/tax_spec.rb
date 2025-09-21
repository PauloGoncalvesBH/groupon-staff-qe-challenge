# frozen_string_literal: true

require 'spec_helper'
require 'json'

RSpec.describe 'Tax Service' do
  describe 'POST /tax/calc' do
    it 'returns 10% tax for USD currency' do
      payload = { amount: 100.0, currency: 'USD', userId: '321312' }.to_json
      post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

      expect(last_response.status).to eq 200
      body = JSON.parse(last_response.body)
      expect(body['taxAmount']).to eq 10.0
      expect(body.keys).to eq(['taxAmount'])
    end

    it 'returns 8% tax for non-USD currency' do
      payload = { amount: 100.0, currency: 'BRL', userId: '123123' }.to_json
      post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

      expect(last_response.status).to eq 200
      body = JSON.parse(last_response.body)
      expect(body['taxAmount']).to eq 8.0
      expect(body.keys).to eq(['taxAmount'])
    end

    it 'return 400 when amount missing' do
      payload = { currency: 'USD', userId: '234234' }.to_json
      post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

      expect(last_response.status).to eq 400
      body = JSON.parse(last_response.body)
      expect(body['error']).to match(/amount is required/)
      expect(body.keys).to eq(['error'])
    end

    it 'return 400 when currency missing' do
      payload = { amount: 50.0, userId: '432432' }.to_json
      post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

      expect(last_response.status).to eq 400
      body = JSON.parse(last_response.body)
      expect(body['error']).to match(/currency is required/)
      expect(body.keys).to eq(['error'])
    end

    it 'return 400 when userId missing' do
      payload = { amount: 50.0, currency: 'USD' }.to_json
      post '/tax/calc', payload, { 'CONTENT_TYPE' => 'application/json' }

      expect(last_response.status).to eq 400
      body = JSON.parse(last_response.body)
      expect(body['error']).to match(/userId is required/)
      expect(body.keys).to eq(['error'])
    end

    it 'return 400 for invalid JSON' do
      post '/tax/calc', '{ bad json', { 'CONTENT_TYPE' => 'application/json' }

      expect(last_response.status).to eq 400
      body = JSON.parse(last_response.body)
      expect(body['error']).to match(/invalid JSON/)
      expect(body.keys).to eq(['error'])
    end
  end

  describe 'GET /health' do
    it 'returns ok' do
      get '/health'
      expect(last_response.status).to eq 200
      body = JSON.parse(last_response.body)
      expect(body['status']).to eq 'ok'
      expect(body).to have_key 'time'
    end
  end
end
