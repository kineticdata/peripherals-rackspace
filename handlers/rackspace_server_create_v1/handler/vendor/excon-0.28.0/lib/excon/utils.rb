module Excon
  module Utils
    extend self

    def valid_connection_keys(params = {})
      Excon::VALID_CONNECTION_KEYS
    end

    def valid_request_keys(params = {})
      Excon::VALID_REQUEST_KEYS
    end

    def connection_uri(datum = @data)
      unless datum
        raise ArgumentError, '`datum` must be given unless called on a Connection'
      end
      if datum[:scheme] == UNIX
        '' << datum[:scheme] << '://' << datum[:socket]
      else
        '' << datum[:scheme] << '://' << datum[:host] << port_string(datum)
      end
    end

    def request_uri(datum)
      connection_uri(datum) << datum[:path] << query_string(datum)
    end

    def port_string(datum)
      if datum[:port].nil? || (datum[:omit_default_port] && ((datum[:scheme].casecmp('http') == 0 && datum[:port] == 80) || (datum[:scheme].casecmp('https') == 0 && datum[:port] == 443)))
        ''
      else
        ':' << datum[:port].to_s
      end
    end

    def query_string(datum)
      str = ''
      case datum[:query]
      when String
        str << '?' << datum[:query]
      when Hash
        str << '?'
        datum[:query].sort_by {|k,_| k.to_s }.each do |key, values|
          if values.nil?
            str << key.to_s << '&'
          else
            [values].flatten.each do |value|
              str << key.to_s << '=' << CGI.escape(value.to_s) << '&'
            end
          end
        end
        str.chop! # remove trailing '&'
      end
      str
    end
  end
end
