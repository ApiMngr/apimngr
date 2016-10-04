var webpack = require('webpack');

var plugins = [
  new webpack.optimize.DedupePlugin(),
  new webpack.optimize.OccurenceOrderPlugin(),
  new webpack.DefinePlugin({
    '__DEV__': process.env.NODE_ENV === 'production',
    'process.env': {
      NODE_ENV: JSON.stringify(process.env.NODE_ENV || 'dev')
    }
  })
];
if (process.env.NODE_ENV === 'production') {
  plugins.push(new webpack.optimize.UglifyJsPlugin({
    sourceMap: false,
    compressor: {
      screw_ie8: false,
      warnings: false
    }
  }));
} else {
  plugins.push(new webpack.HotModuleReplacementPlugin());
  plugins.push(new webpack.NoErrorsPlugin());
}

module.exports = {
  output: {
    path: '../public/javascripts/bundle/',
    publicPath: '/assets/javascripts/bundle/',
    filename: '[name].js',
    library: 'RAM',
    libraryTarget: 'umd'
  },
  externals: {
    // "jquery": "jQuery",
  },
  entry: {
    bundle: './src/index.js',
  },
  resolve: {
    extensions: ['', '.js', '.jsx', 'es6', '.css', '.less']
  },
  devServer: {
    port: 8080,
  },
  module: {
    loaders: [
      {
        test: /\.js|\.jsx|\.es6$/,
        exclude: /node_modules/,
        loader: 'babel-loader'
      },
      {
        test: /node_modules\/auth0-lock\/.*\.js$/,
        loaders: [
          'transform-loader/cacheable?brfs',
          'transform-loader/cacheable?packageify'
        ]
      },
      {
        test: /node_modules\/auth0-lock\/.*\.ejs$/,
        loader: 'transform-loader/cacheable?ejsify'
      },
      {
        test: /\.json$/,
        loader: 'json-loader'
      },
      {
        test: /\.less$/,
        loader: "style!css!less"
      },
      {
        test: /\.css$/,
        exclude: /\.useable\.css$/,
        loader: "style!css"
      },
      {
        test: /\.useable\.css$/,
        loader: "style/useable!css"
      }
    ]
  },
  plugins: plugins
};