
# Load the JRuby Open SSL library unless it has already been loaded.  This
# prevents multiple handlers using the same library from causing problems.
if not defined?(Jopenssl)
  # Load the Bouncy Castle library unless it has already been loaded.  This
  # prevents multiple handlers using the same library from causing problems.
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/bouncy-castle-java-1.5.0147/lib')
  $:.unshift library_path
  # Require the library
  require 'bouncy-castle-java'
  
  
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/jruby-openssl-0.8.8/lib/shared')
  $:.unshift library_path
  # Require the library
  require 'openssl'
  # Require the version constant
  require 'jopenssl/version'
end

# Validate the the loaded openssl library is the library that is expected for
# this handler to execute properly.
if not defined?(Jopenssl::Version::VERSION)
  raise "The Jopenssl class does not define the expected VERSION constant."
elsif Jopenssl::Version::VERSION != '0.8.8'
  raise "Incompatible library version #{Jopenssl::Version::VERSION} for Jopenssl.  Expecting version 0.8.8"
end




# Load the ruby JSON library (used by the Octokit library) unless 
# it has already been loaded.  This prevents multiple handlers using the same 
# library from causing problems.
if not defined?(JSON)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/json-1.8.0/lib')
  $:.unshift library_path
  # Require the library
  require 'json'
end

# Validate the the loaded JSON library is the library that is expected for
# this handler to execute properly.
if not defined?(JSON::VERSION)
  raise "The JSON class does not define the expected VERSION constant."
elsif JSON::VERSION.to_s != '1.8.0'
  raise "Incompatible library version #{JSON::VERSION} for JSON.  Expecting version 1.8.0."
end





if not defined?(Net::SSH)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/net-ssh')
  $:.unshift library_path
  # Require the library
  require 'net/ssh'
end

# Validate the the loaded openssl library is the library that is expected for
# this handler to execute properly.
if not defined?(Net::SSH::Version::STRING)
  raise "The Net-SSH class does not define the expected VERSION constant."
elsif Net::SSH::Version::STRING.to_s != '2.6.7'
  raise "Incompatible library version #{Net::SSH::Version::STRING} for Net-SSH.  Expecting version 2.6.7."
end



if not defined?(Net::SCP)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/net-scp-1.1.2/lib')
  $:.unshift library_path
  # Require the library
  require 'net/scp'
  require 'net/scp/version'
end

# Validate the the loaded openssl library is the library that is expected for
# this handler to execute properly.
if not defined?(Net::SSH::Version::STRING)
  raise "The Net-SSH class does not define the expected VERSION constant."
elsif Net::SCP::Version::STRING.to_s != '1.1.2'
  raise "Incompatible library version #{Net::SSH::Version::STRING} for Net-SCP.  Expecting version 1.1.2."
end




# Load the ruby Excon library unless it has already been loaded.  This prevents
# multiple handlers using the same library from causing problems.
if not defined?(Excon)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/excon-0.28.0/lib')
  $:.unshift library_path
  # Require the library
  require 'excon'
end

# Validate the the loaded Excon library is the library that is expected for
# this handler to execute properly.
if not defined?(Excon::VERSION)
  raise "The Excon class does not define the expected VERSION constant."
elsif Excon::VERSION != '0.28.0'
  raise "Incompatible library version #{Excon::VERSION} for Excon.  Expecting version 0.28.0."
end




# Load the ruby Formatador library unless it has already been loaded.  This  
# prevents multiple handlers using the same library from causing problems.
if not defined?(Formatador)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/formatador-0.2.4/lib')
  $:.unshift library_path
  # Require the library
  require 'formatador'
end

# Validate the the loaded Formatador library is the library that is expected for
# this handler to execute properly.
if not defined?(Formatador::VERSION)
  raise "The Excon class does not define the expected VERSION constant."
elsif Formatador::VERSION != '0.2.4'
  raise "Incompatible library version #{Formatador::VERSION} for Excon.  Expecting version 0.2.4."
end




# Load the ruby Mime Types library unless it has already been loaded.  This prevents
# multiple handlers using the same library from causing problems.
if not defined?(MIME)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/mime-types-1.19/lib/')
  $:.unshift library_path
  # Require the library
  require 'mime/types'
end

# Validate the the loaded Mime Types library is the library that is expected for
# this handler to execute properly.
if not defined?(MIME::Types::VERSION)
  raise "The Mime class does not define the expected VERSION constant."
elsif MIME::Types::VERSION != '1.19'
  raise "Incompatible library version #{MIME::Types::VERSION} for Mime Types.  Expecting version 1.19."
end




# Load the ruby Mini Portile library unless it has already been loaded.  This  
# prevents multiple handlers using the same library from causing problems.
if not defined?(MiniPortile)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/mini_portile-0.5.1/lib/')
  $:.unshift library_path
  # Require the library
  require 'mini_portile'
end

# Validate the the loaded Mini Portile library is the library that is expected 
# for this handler to execute properly.
if not defined?(MiniPortile::VERSION)
  raise "The Mini Portile class does not define the expected VERSION constant."
elsif MiniPortile::VERSION != '0.5.1'
  raise "Incompatible library version #{MiniPortile::VERSION} for Mini Portile.  Expecting version 0.5.1."
end





# Load the ruby Builder library unless it has already been loaded.  This prevents
# multiple handlers using the same library from causing problems.
if not defined?(Builder)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/builder-3.0.0/lib')
  $:.unshift library_path
  # Require the library
  require 'builder'
end

# Validate the the loaded Builder library is the library that is expected for
# this handler to execute properly.
if not defined?(Builder::VERSION)
  raise "The Builder class does not define the expected VERSION constant."
elsif Builder::VERSION != '3.0.0'
  raise "Incompatible library version #{Builder::VERSION} for Builder.  Expecting version 3.0.0."
end




# Load the ruby Ruby HMAC library unless it has already been loaded.  This prevents
# multiple handlers using the same library from causing problems.
if not defined?(HMAC)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/ruby-hmac-0.4.0/lib')
  $:.unshift library_path
  # Require the library
  require 'hmac'
end

# Validate the the loaded Builder library is the library that is expected for
# this handler to execute properly.
if not defined?(HMAC::VERSION)
  raise "The Ruby HMAC class does not define the expected VERSION constant."
elsif HMAC::VERSION != '0.4.0'
  raise "Incompatible library version #{HMAC::VERSION} for Ruby HMAC.  Expecting version 0.4.0."
end





# Load the ruby Multijson library (used by the Octokit library) unless 
# it has already been loaded.  This prevents multiple handlers using the same 
# library from causing problems.
if not defined?(MultiJson)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/multi_json-1.7.5/lib')
  $:.unshift library_path
  # Require the library
  require 'multi_json'
end

# Validate the the loaded MultiJson library is the library that is expected for
# this handler to execute properly.
if not defined?(MultiJson::Version)
  raise "The MultiJson class does not define the expected VERSION constant."
elsif MultiJson::Version.to_s != '1.7.5'
  raise "Incompatible library version #{MultiJson::Version} for MultiJson.  Expecting version 1.7.5."
end




# Load the ruby Nokogiri library unless it has already been loaded.  This prevents
# multiple handlers using the same library from causing problems.
if not defined?(Nokogiri)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/nokogiri-1.5.3-java/lib')
  $:.unshift library_path
  # Require the library
  require 'nokogiri'
  require 'nokogiri/version'
end

# Validate the the loaded Nokogiri library is the library that is expected for
# this handler to execute properly.
if not defined?(Nokogiri::VERSION)
  raise "The Nokogiri class does not define the expected VERSION constant."
elsif Nokogiri::VERSION != '1.5.3'
  raise "Incompatible library version #{Nokogiri::VERSION} for Nokogiri.  Expecting version 1.5.3."
end




# Load the ruby Fog library unless it has already been loaded.  This prevents
# multiple handlers using the same library from causing problems.
if not defined?(Fog)
  # Calculate the location of this file
  handler_path = File.expand_path(File.dirname(__FILE__))
  # Calculate the location of our library and add it to the Ruby load path
  library_path = File.join(handler_path, 'vendor/fog-1.18.0/lib')
  $:.unshift library_path
  # Require the library
  require 'fog'
end

# Validate the the loaded Fog library is the library that is expected for
# this handler to execute properly.
if not defined?(Fog::VERSION)
  raise "The Fog class does not define the expected VERSION constant."
elsif Fog::VERSION != '1.18.0'
  raise "Incompatible library version #{Fog::VERSION} for Fog.  Expecting version 1.18.0."
end


