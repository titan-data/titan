#
# Sphinx Configuration for Titan documentation
#

import sphinx_rtd_theme

# -- Project information -----------------------------------------------------

project = u'titan'
copyright = u'2021, Titan Project Contributors'
author = u'Titan Project Contributors'

# -- Project configuration ---------------------------------------------------

def setup(app):
  app.add_config_value('release_type', '', 'env')

release_type = "development"
version = "latest"


# -- General configuration ---------------------------------------------------

extensions = ['recommonmark', 'sphinx.ext.ifconfig']
templates_path = ['_templates']
source_suffix = ['.rst', '.md']
exclude_patterns = ['Thumbs.db', '.DS_Store']
language = None

# -- Options for HTML output -------------------------------------------------

html_theme = 'sphinx_rtd_theme'
html_theme_path = [sphinx_rtd_theme.get_html_theme_path()]
html_style = 'css/titan.css'
html_static_path = ['_static']
html_show_sourcelink = False
